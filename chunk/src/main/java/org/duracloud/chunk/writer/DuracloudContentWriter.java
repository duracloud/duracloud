/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.writer;

import org.apache.commons.io.FileUtils;
import org.duracloud.chunk.ChunkableContent;
import org.duracloud.chunk.error.ContentNotAddedException;
import org.duracloud.chunk.error.NotFoundException;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.stream.ChunkInputStream;
import org.duracloud.chunk.stream.KnownLengthInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.common.retry.Retriable;
import org.duracloud.common.retry.Retrier;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class implements the ContentWriter interface to write the provided
 * content to the Duracloud storeclient interface.
 *
 * @author Andrew Woods
 *         Date: Feb 5, 2010
 */
public class DuracloudContentWriter implements ContentWriter {

    private final Logger log = LoggerFactory.getLogger(DuracloudContentWriter.class);

    private ContentStore contentStore;
    private String username;
    private Set<String> existingSpaces = new HashSet<String>();
    private List<AddContentResult> results = new ArrayList<AddContentResult>();
    private ChecksumUtil checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);

    // if true, skip writing results and throw exception when errors occur
    private boolean throwOnError = false;

    // if true, skip checks for chunks in storage
    private boolean jumpStart = false;
    
    private static int DEFAULT_MAX_RETRIES = 4;  //actual attempts will always equal MAX_RETRIES+1
    private static int DEFAULT_WAIT_IN_MS_BETWEEN_RETRIES = 1000;
    
    private int maxRetries = DEFAULT_MAX_RETRIES;
    private int waitInMsBetweenRetries = DEFAULT_MAX_RETRIES;
    
    public DuracloudContentWriter(ContentStore contentStore, String username) {
        this(contentStore,
             username,
             DEFAULT_MAX_RETRIES,
             DEFAULT_WAIT_IN_MS_BETWEEN_RETRIES);
    }
    
    public DuracloudContentWriter(ContentStore contentStore,
                                  String username,
                                  int maxRetries,
                                  int waitInMsBetweenRetries) {
        this.contentStore = contentStore;
        this.username = username;
        this.maxRetries = maxRetries;
        this.waitInMsBetweenRetries = waitInMsBetweenRetries;
    }

    public DuracloudContentWriter(ContentStore contentStore,
                                  String username,
                                  boolean throwOnError,
                                  boolean jumpStart) {
        this(contentStore,
             username,
             throwOnError,
             jumpStart,
             DEFAULT_MAX_RETRIES,
             DEFAULT_WAIT_IN_MS_BETWEEN_RETRIES);
    }
    
    public DuracloudContentWriter(ContentStore contentStore,
                                  String username,
                                  boolean throwOnError,
                                  boolean jumpStart,
                                  int maxRetries, 
                                  int waitInMsBetweenRetries) {
        this(contentStore, username, maxRetries, waitInMsBetweenRetries);
        this.throwOnError = throwOnError;
        this.jumpStart = jumpStart;
    }

    public int getMaxRetries(){
        return this.maxRetries;
    }

    public List<AddContentResult> getResults() {
        return results;
    }

    public void ignore(String spaceId, String contentId, long contentSize) {
        AddContentResult result = new AddContentResult(spaceId,
                                                       contentId,
                                                       contentSize);
        result.setState(AddContentResult.State.IGNORED);
        results.add(result);
    }

    /**
     * This method implements the ContentWriter interface for writing content
     * to a DataStore. In this case, the DataStore is durastore.
     *
     * @param spaceId   destination space of arg chunkable content
     * @param chunkable content to be written
     * @throws NotFoundException if space is not found
     */
    @Override
    public ChunksManifest write(String spaceId,
                                ChunkableContent chunkable,
                                Map<String, String> contentProperties)
        throws NotFoundException {
        return write(spaceId, chunkable, contentProperties, true);
    }
    
    private ChunksManifest write(String spaceId,
                                ChunkableContent chunkable,
                                Map<String, String> contentProperties, 
                                boolean lastAttempt)
        throws NotFoundException {
        log.debug("write: " + spaceId);
        createSpaceIfNotExist(spaceId);
        boolean errorsExist = false;
        
        for (ChunkInputStream chunk : chunkable) {
            writeChunk(spaceId, chunk);
            
            if(errorsExist = errorsExist()){
                break;
            }
        }

        ChunksManifest manifest = chunkable.finalizeManifest();

        if(!errorsExist){
            addManifest(spaceId, manifest, contentProperties,lastAttempt);
        }

        log.debug("written: " + spaceId + ", " + manifest.getManifestId());
        return manifest;
    }

    protected boolean errorsExist() {
        boolean containsErrors = false;
        for(AddContentResult result : this.results){
            if(result.getState().equals(AddContentResult.State.ERROR)){
                containsErrors = true;
                break;
            }
        }
        return containsErrors;
    }

    /*
     * Writes chunk to DuraCloud if it does not already exist in DuraCloud with a
     * matching checksum. Retry failed transfers.
     */
    private void writeChunk(String spaceId, ChunkInputStream chunk)
        throws NotFoundException {
        // Write chunk as a temp file
        String chunkId = chunk.getChunkId();
        File chunkFile = IOUtil.writeStreamToFile(chunk);

        try {
            String chunkChecksum = getChunkChecksum(chunkFile);

            // Write chunk if it is not already in storage (or jumpstart is enabled)
            if (jumpStart || !chunkInStorage(spaceId, chunkId, chunkChecksum)) {
                try {
                    createRetrier().execute(new Retriable() {
                        private int attempt = 0;
                        @Override
                        public Object retry() throws Exception {
                            attempt++;
                            try(InputStream chunkStream = new FileInputStream(chunkFile)) {
                                ChunkInputStream chunkFileStream =
                                    new ChunkInputStream(chunkId,
                                                         chunkStream,
                                                         chunkFile.length(),
                                                         chunk.md5Preserved());
                                writeSingle(spaceId, chunkChecksum, chunkFileStream, attempt == getMaxRetries()+1);
                            }
                            return "";
                        }
                    });
                        
                } catch (Exception e) {
                    String err = "Failed to store chunk with ID " + chunkId +
                                 " in space " + spaceId + " after " + getMaxRetries() +
                                 " attempts. Last error: " + e.getMessage();
                    throw new DuraCloudRuntimeException(err, e);
                }
            }
        } finally {
            if(null != chunkFile && chunkFile.exists()) {
                FileUtils.deleteQuietly(chunkFile);
            }
        }
    }

    /*
     * Determine the checksum of the chunk file
     */
    private String getChunkChecksum(File chunkFile) {
        try {
            return checksumUtil.generateChecksum(chunkFile);
        } catch(IOException e) {
            throw new DuraCloudRuntimeException("Unable to generate checksum for file " +
                                                chunkFile + " due to: " + e.getMessage());
        }
    }

    protected void setChecksumUtil(ChecksumUtil checksumUtil) {
        this.checksumUtil = checksumUtil;
    }

    /*
     * Determines if a file chunk exists in DuraCloud storage with the given checksum
     */
    private boolean chunkInStorage(String spaceId, String contentId, String checksum) {
        try {
            if (contentStore.contentExists(spaceId, contentId)) { // dc file exists
                Map<String, String> props =
                    contentStore.getContentProperties(spaceId, contentId);
                String dcChecksum = props.get(ContentStore.CONTENT_CHECKSUM);
                if (null != checksum && null != dcChecksum && checksum.equals(dcChecksum)) {
                    return true; // File with matching checksum already in DuraCloud
                } else {
                    return false; // File exists in DuraCloud, but checksums don't match
                }
            } else {
                return false; // File does not exist in DuraCloud
            }
        } catch (ContentStoreException e) {
            return false; // File does not exist in DuraCloud
        }
    }

    @Override
    public ChunksManifest write(String spaceId, ChunkableContent chunkable)
        throws NotFoundException {
        return write(spaceId, chunkable, null);
    }

    /**
     * This method writes a single chunk to the DataStore.
     *
     * @param spaceId destination where arg chunk content will be written
     * @param chunkChecksum md5 checksum of the chunk if known, null otherwise
     * @param chunk   content to be written
     * @param properties user-defined properties for the content
     * @return MD5 of written content
     * @throws NotFoundException if space is not found
     */
    @Override
    public String writeSingle(String spaceId,
                              String chunkChecksum,
                              ChunkInputStream chunk,
                              Map<String,String> properties)
        throws NotFoundException {

        log.debug("writeSingle: " + spaceId + ", " + chunk.getChunkId());
        createSpaceIfNotExist(spaceId);

        addChunk(spaceId, chunkChecksum, chunk, properties, true);

        log.debug("written: " + spaceId + ", " + chunk.getChunkId());
        return chunk.getMD5();
    }

    @Override
    public String writeSingle(String spaceId,
                              String chunkChecksum,
                              ChunkInputStream chunk)
        throws NotFoundException {
        return writeSingle(spaceId, chunkChecksum, chunk, true);
    }

    private String writeSingle(String spaceId,
                              String chunkChecksum,
                              ChunkInputStream chunk,
                              boolean lastAttempt)
        throws NotFoundException {
        log.debug("writeSingle: " + spaceId + ", " + chunk.getChunkId());
        createSpaceIfNotExist(spaceId);

        addChunk(spaceId, chunkChecksum, chunk, null, lastAttempt);

        log.debug("written: " + spaceId + ", " + chunk.getChunkId());
        return chunk.getMD5();
    }

    private void addChunk(String spaceId,
                          String chunkChecksum,
                          ChunkInputStream chunk,
                          Map<String,String> properties,
                          boolean lastAttempt) {
        String chunkId = chunk.getChunkId();
        log.debug("addChunk: " + spaceId + ", " + chunkId);

        addContentThenReport(spaceId,
                             chunkId,
                             chunk,
                             chunk.getChunkSize(),
                             chunk.getMimetype(),
                             chunkChecksum,
                             properties, 
                             lastAttempt);
    }

    private void addManifest(final String spaceId,
                             final ChunksManifest manifest,
                             final Map<String, String> properties,
                             boolean lastAttempt) {
        String manifestId = manifest.getManifestId();
        log.debug("addManifest: " + spaceId + ", " + manifestId);
        try {
            createRetrier().execute(() -> {
                try (KnownLengthInputStream manifestBody = manifest.getBody()) {
                    String manifestChecksum =
                        checksumUtil.generateChecksum(manifest.getBody());
                    int manifestLength = manifestBody.getLength();
                    addContentThenReport(spaceId,
                                         manifestId,
                                         manifestBody,
                                         manifestLength,
                                         manifest.getMimetype(),
                                         manifestChecksum,
                                         properties,
                                         lastAttempt);
                }
                return "";
            });
        } catch (Exception e) {
            String err =
                "Failed to add manifest " + manifestId
                         + " afert multiple retries: "
                         + e.getMessage();
            throw new DuraCloudRuntimeException(err, e);
        }
    }

    private Retrier createRetrier() {
        return new Retrier(maxRetries, waitInMsBetweenRetries, 1);
    }

    private void addContentThenReport(String spaceId,
                                      String contentId,
                                      InputStream contentStream,
                                      long contentSize,
                                      String contentMimetype,
                                      String contentChecksum, 
                                      Map<String,String> properties,
                                      boolean lastAttempt) {
        AddContentResult result = new AddContentResult(spaceId,
                                                       contentId,
                                                       contentSize);
        String md5 = null;
        try {
            md5 = addContent(spaceId,
                             contentId,
                             contentStream,
                             contentSize,
                             contentMimetype,
                             contentChecksum, 
                             properties);
        } catch (ContentNotAddedException e) {
            if (throwOnError) {
                String err = "Content not added due to: " + e.getMessage();
                throw new DuraCloudRuntimeException(err, e);
            } else {
                if(lastAttempt){
                    result.setState(AddContentResult.State.ERROR);
                } else {
                    String err = "Content not added due to: " + e.getMessage();
                    throw new DuraCloudRuntimeException(err, e);
                }
            }
        }

        if (!throwOnError) {
            if (md5 != null) {
                result.setMd5(md5);
                result.setState(AddContentResult.State.SUCCESS);
            }
            results.add(result);
        }
    }

    /**
     * @return MD5 of added content
     * @throws ContentNotAddedException
     */
    private String addContent(String spaceId,
                              String contentId,
                              InputStream contentStream,
                              long contentSize,
                              String contentMimetype,
                              String contentChecksum,
                              Map<String,String> properties)
        throws ContentNotAddedException {
        
        if (properties == null) {
            properties = new HashMap<String, String>();
        }
        
        if (!properties.containsKey(StorageProvider.PROPERTIES_CONTENT_CREATOR)) {
            properties.put(StorageProvider.PROPERTIES_CONTENT_CREATOR, username);
        }

        try {
            return contentStore.addContent(spaceId,
                                           contentId,
                                           contentStream,
                                           contentSize,
                                           contentMimetype,
                                           contentChecksum,
                                           properties);
        } catch (ContentStoreException e) {
            log.error(e.getFormattedMessage(), e);
            throw new ContentNotAddedException(spaceId, contentId, e);
        } catch (Exception ex) {
            log.error("Error adding content:" + ex.getMessage(), ex);
            throw new ContentNotAddedException(spaceId, contentId, ex);
        }
    }

    private void createSpaceIfNotExist(String spaceId)
        throws NotFoundException {

        if (existingSpaces.contains(spaceId)) {
            return;
        }

        if (!spaceExists(spaceId)) {
            createSpace(spaceId);
        }

        int tries = 0;
        boolean exists;
        while (!(exists = spaceExists(spaceId)) && tries++ < 10) {
            sleep(1000);
        }

        if (!exists) {
            throw new NotFoundException("Space not found: " + spaceId);
        }

        existingSpaces.add(spaceId);
    }

    private void createSpace(String spaceId) {
        try {
            contentStore.createSpace(spaceId);
        } catch (ContentStoreException e) {
            // do nothing.
        }
    }

    private boolean spaceExists(String spaceId) {
        try {
            return null != contentStore.getSpaceACLs(spaceId);
        } catch (ContentStoreException e) {
            return false;
        }
    }

    private void sleep(long napTime) {
        try {
            Thread.sleep(napTime);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

}