/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.writer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.chunk.ChunkableContent;
import org.duracloud.chunk.error.ContentNotAddedException;
import org.duracloud.chunk.error.NotFoundException;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.stream.ChunkInputStream;
import org.duracloud.chunk.stream.KnownLengthInputStream;
import org.duracloud.client.ContentStore;
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

    public DuracloudContentWriter(ContentStore contentStore, String username) {
        this.contentStore = contentStore;
        this.username = username;
    }

    public DuracloudContentWriter(ContentStore contentStore,
                                  String username,
                                  boolean throwOnError,
                                  boolean jumpStart) {
        this(contentStore, username);
        this.throwOnError = throwOnError;
        this.jumpStart = jumpStart;
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
        log.debug("write: " + spaceId);
        createSpaceIfNotExist(spaceId);

        for (ChunkInputStream chunk : chunkable) {
            writeChunk(spaceId, chunk);
        }

        ChunksManifest manifest = chunkable.finalizeManifest();
        addManifest(spaceId, manifest, contentProperties);

        log.debug("written: " + spaceId + ", " + manifest.getManifestId());
        return manifest;
    }

    /*
     * Writes chunk to DuraCloud if it does not already exist in DuraCloud with a
     * matching checksum. Retry failed transfers.
     */
    private void writeChunk(String spaceId, ChunkInputStream chunk)
        throws NotFoundException {
        String chunkId = chunk.getChunkId();

        
        File chunkFile = null;
        boolean exists = false;
        String chunkChecksum = null;
        // check if chunk exists in content store.
        try {
            if(!jumpStart){
                exists =  this.contentStore.contentExists(spaceId, chunkId);
            }
        } catch (Exception e1) {
            String message = "failed to check for existence of chunk " + chunkId
                + " in space "
                + spaceId;
            log.error(message,
                      e1.getMessage());
            throw new DuraCloudRuntimeException(message,e1);
        }
            

        int maxRetries = 5;

        try {
            // Write chunk as a temp file if no jumpstart and a content item already exists
            // 
            if(exists){
                chunkFile = IOUtil.writeStreamToFile(chunk);
                chunkChecksum = getChunkChecksum(chunkFile);
            }


            // Write chunk if it is not already in storage (or jumpstart is enabled)
            if (jumpStart || !exists || !chunkInStorage(spaceId, chunkId, chunkChecksum)) {
                final File chunkFile1 = chunkFile;
                final String chunkChecksum1 = chunkChecksum;
                try {
                    new Retrier(maxRetries).execute(() -> {
                        InputStream chunkStream = null;
                        try{
                            ChunkInputStream chunkInputStream =
                                chunkFile1 != null ?
                                new ChunkInputStream(chunkId,
                                                     chunkStream = new FileInputStream(chunkFile1),
                                                     chunk.getChunkSize(),
                                                     chunk.md5Preserved()) : chunk;
                                                                                  
                            writeSingle(spaceId, chunkChecksum1, chunkInputStream);
                        }finally{
                            IOUtils.closeQuietly(chunkStream);
                        }
                        return "";
                    });
                } catch (Exception e) {
                    String err = "Failed to store chunk with ID " + chunkId +
                                 " in space " + spaceId + " after " + maxRetries +
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
     * Determines if an existing chunk in DuraCloud storage matches the given  checksum
     */
    private boolean chunkInStorage(String spaceId, String contentId, String checksum) {
        try {
                Map<String, String> props =
                    contentStore.getContentProperties(spaceId, contentId);
                String dcChecksum = props.get(ContentStore.CONTENT_CHECKSUM);
                if(null != checksum && null != dcChecksum && checksum.equals(dcChecksum)) {
                    return true; // File with matching checksum already in DuraCloud
                } else {
                    return false; // File exists in DuraCloud, but checksums don't match
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

        addChunk(spaceId, chunkChecksum, chunk, properties);

        log.debug("written: " + spaceId + ", " + chunk.getChunkId());
        return chunk.getMD5();
    }

    @Override
    public String writeSingle(String spaceId,
                              String chunkChecksum,
                              ChunkInputStream chunk)
        throws NotFoundException {
        log.debug("writeSingle: " + spaceId + ", " + chunk.getChunkId());
        createSpaceIfNotExist(spaceId);

        addChunk(spaceId, chunkChecksum, chunk, null);

        log.debug("written: " + spaceId + ", " + chunk.getChunkId());
        return chunk.getMD5();
    }

    private void addChunk(String spaceId,
                          String chunkChecksum,
                          ChunkInputStream chunk,
                          Map<String,String> properties) {
        String chunkId = chunk.getChunkId();
        log.debug("addChunk: " + spaceId + ", " + chunkId);

        addContentThenReport(spaceId,
                             chunkId,
                             chunk,
                             chunk.getChunkSize(),
                             chunk.getMimetype(),
                             chunkChecksum,
                             properties);
    }

    private void addManifest(String spaceId,
                             ChunksManifest manifest,
                             Map<String, String> properties) {
        String manifestId = manifest.getManifestId();
        log.debug("addManifest: " + spaceId + ", " + manifestId);

        KnownLengthInputStream manifestBody = manifest.getBody();
        String manifestChecksum = checksumUtil.generateChecksum(manifest.getBody());
        addContentThenReport(spaceId,
                             manifestId,
                             manifestBody,
                             manifestBody.getLength(),
                             manifest.getMimetype(),
                             manifestChecksum,
                             properties);
    }

    private void addContentThenReport(String spaceId,
                                      String contentId,
                                      InputStream contentStream,
                                      long contentSize,
                                      String contentMimetype,
                                      String contentChecksum, 
                                      Map<String,String> properties) {
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
            if(throwOnError) {
                String err = "Content not added due to: " + e.getMessage();
                throw new DuraCloudRuntimeException(err, e);
            } else {
                result.setState(AddContentResult.State.ERROR);
            }
        }

        if(!throwOnError) {
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
        
        if(properties == null){
            properties = new HashMap<String, String>();
        }
        
        if(!properties.containsKey(StorageProvider.PROPERTIES_CONTENT_CREATOR)){
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