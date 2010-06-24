/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.writer;

import org.duracloud.chunk.ChunkableContent;
import org.duracloud.chunk.error.ContentNotAddedException;
import org.duracloud.chunk.error.NotFoundException;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.stream.ChunkInputStream;
import org.duracloud.chunk.stream.KnownLengthInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
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
    private Set<String> existingSpaces = new HashSet<String>();
    private List<AddContentResult> results = new ArrayList<AddContentResult>();

    // if true, skip writing results and throw exception when errors occur
    private boolean throwOnError = false;

    public DuracloudContentWriter(ContentStore contentStore) {
        this.contentStore = contentStore;
    }

    public DuracloudContentWriter(ContentStore contentStore,
                                  boolean throwOnError) {
        this.contentStore = contentStore;
        this.throwOnError = throwOnError;
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
    public ChunksManifest write(String spaceId, ChunkableContent chunkable)
        throws NotFoundException {
        log.debug("write: " + spaceId);
        createSpaceIfNotExist(spaceId);

        for (ChunkInputStream chunk : chunkable) {
            writeSingle(spaceId, null, chunk);
        }

        ChunksManifest manifest = chunkable.finalizeManifest();
        addManifest(spaceId, manifest);

        log.debug("written: " + spaceId + ", " + manifest.getManifestId());
        return manifest;
    }

    /**
     * This method writes a single chunk to the DataStore.
     *
     * @param spaceId destination where arg chunk content will be written
     * @param chunkChecksum md5 checksum of the chunk if known, null otherwise
     * @param chunk   content to be written
     * @return MD5 of written content
     * @throws NotFoundException if space is not found
     */
    public String writeSingle(String spaceId,
                              String chunkChecksum,
                              ChunkInputStream chunk)
        throws NotFoundException {
        log.debug("writeSingle: " + spaceId + ", " + chunk.getChunkId());
        createSpaceIfNotExist(spaceId);

        addChunk(spaceId, chunkChecksum, chunk);

        log.debug("written: " + spaceId + ", " + chunk.getChunkId());
        return chunk.getMD5();
    }

    private void addChunk(String spaceId,
                          String chunkChecksum,
                          ChunkInputStream chunk) {
        String chunkId = chunk.getChunkId();
        log.debug("addChunk: " + spaceId + ", " + chunkId);

        addContentThenReport(spaceId,
                             chunkId,
                             chunk,
                             chunk.getChunkSize(),
                             chunk.getMimetype(),
                             chunkChecksum);
    }

    private void addManifest(String spaceId, ChunksManifest manifest) {
        String manifestId = manifest.getManifestId();
        log.debug("addManifest: " + spaceId + ", " + manifestId);

        KnownLengthInputStream manifestBody = manifest.getBody();
        addContentThenReport(spaceId,
                             manifestId,
                             manifestBody,
                             manifestBody.getLength(),
                             manifest.getMimetype(),
                             null);
    }

    private void addContentThenReport(String spaceId,
                                      String contentId,
                                      InputStream contentStream,
                                      long contentSize,
                                      String contentMimetype,
                                      String contentChecksum) {
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
                             contentChecksum);
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
                              String contentChecksum)
        throws ContentNotAddedException {
        Map<String, String> metadata = null;
        try {
            return contentStore.addContent(spaceId,
                                           contentId,
                                           contentStream,
                                           contentSize,
                                           contentMimetype,
                                           contentChecksum,
                                           metadata);
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

        // If space already exists, will not be recreated
        createSpace(spaceId);

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
        Map<String, String> metadata = null;
        try {
            contentStore.createSpace(spaceId, metadata);
        } catch (ContentStoreException e) {
            // do nothing.
        }
    }

    private boolean spaceExists(String spaceId) {
        try {
            return null != contentStore.getSpaceAccess(spaceId);
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