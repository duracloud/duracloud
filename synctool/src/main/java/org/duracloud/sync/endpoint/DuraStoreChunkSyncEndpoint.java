/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.duracloud.chunk.FileChunker;
import org.duracloud.chunk.FileChunkerOptions;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.ChunksManifestBean;
import org.duracloud.chunk.writer.DuracloudContentWriter;
import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.stitch.FileStitcher;
import org.duracloud.stitch.datasource.impl.DuraStoreDataSource;
import org.duracloud.stitch.impl.FileStitcherImpl;
import org.duracloud.sync.config.SyncToolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Apr 8, 2010
 */
public class DuraStoreChunkSyncEndpoint extends DuraStoreSyncEndpoint {

    private final Logger log = LoggerFactory.getLogger(
        DuraStoreChunkSyncEndpoint.class);

    private FileChunker chunker;
    private FileStitcher stitcher;

    public DuraStoreChunkSyncEndpoint(ContentStore contentStore,
                                      String username,
                                      String spaceId,
                                      boolean syncDeletes,
                                      boolean jumpStart,
                                      long maxFileSize) {
        this(contentStore,
             username,
             spaceId,
             syncDeletes,
             maxFileSize,
             true,
             false,
             jumpStart,
             SyncToolConfig.DEFAULT_UPDATE_SUFFIX,
             null);
    }
    
    public DuraStoreChunkSyncEndpoint(ContentStore contentStore,
                                      String username,
                                      String spaceId,
                                      boolean syncDeletes,
                                      long maxFileSize,
                                      boolean syncUpdates, 
                                      boolean renameUpdates,
                                      boolean jumpStart,
                                      String updateSuffix,
                                      String prefix) {
        super(contentStore,
              username,
              spaceId,
              syncDeletes,
              syncUpdates,
              renameUpdates,
              jumpStart,
              updateSuffix,
              prefix);

        if (maxFileSize % 1024 != 0) {
            throw new RuntimeException("Max file size must be factor of 1024");
        }

        DuracloudContentWriter contentWriter =
            new DuracloudContentWriter(contentStore, username, true);
        FileChunkerOptions chunkerOptions = new FileChunkerOptions(maxFileSize);

        chunker = new FileChunker(contentWriter, chunkerOptions);
        stitcher = new FileStitcherImpl(new DuraStoreDataSource(contentStore));
    }

    @Override
    protected Map<String, String> getContentProperties(String spaceId,
                                                       String contentId) {
        Map<String, String> props = super.getContentProperties(spaceId,
                                                               contentId);
        if (null == props) {
            props = getPropertiesIfChunked(spaceId, contentId);
        }

        return props;
    }

    private Map<String, String> getPropertiesIfChunked(String spaceId,
                                                       String contentId) {
        Map<String, String> props = null;
        String manifestId = contentId + ChunksManifest.manifestSuffix;
        try {
            Content manifest = stitcher.getContentFromManifest(spaceId,
                                                               manifestId);
            props = manifest.getProperties();
            log.info("Manifest found for content: {}/{}", spaceId, contentId);

        } catch (Exception e) {
            log.debug("Not a chunked content item: {}/{}", spaceId, contentId);
        }

        return props;
    }

    @Override
    public void deleteContent(String spaceId, String contentId)
        throws ContentStoreException {
        boolean contentDeleted = true;
        try {
            super.deleteContent(spaceId, contentId);

        } catch (ContentStoreException e) {
            contentDeleted = false;
        }

        if (!contentDeleted) {
            log.debug("Maybe content was chunked? {}/{}", spaceId, contentId);
            ChunksManifest manifest = getManifest(spaceId, contentId);

            if (null != manifest) {
                String manifestId = manifest.getManifestId();

                log.info("Deleting all chunks in manifest, {}", manifestId);
                for (ChunksManifestBean.ManifestEntry entry : manifest.getEntries()) {
                    super.deleteContent(spaceId, entry.getChunkId());
                }

                log.info("Deleting manifest: {}/{}", spaceId, manifestId);
                super.deleteContent(spaceId, manifestId);
            }
        }
    }

    private ChunksManifest getManifest(String spaceId, String contentId) {
        String manifestId = contentId + ChunksManifest.manifestSuffix;
        ChunksManifest manifest = null;
        try {
            manifest = stitcher.getManifest(spaceId, manifestId);

        } catch (Exception e) {
            log.info("No manifest for item: {}/{}", spaceId, contentId);
        }
        return manifest;
    }

    @Override
    protected void addUpdateContent(String contentId,
                                    MonitoredFile syncFile) {
        Map<String,String> properties = createProps(syncFile.getAbsolutePath(),getUsername());
        chunker.addContent(getSpaceId(),
                           contentId,
                           syncFile.getChecksum(),
                           syncFile.length(),
                           syncFile.getStream(),
                           properties);
    }

    @Override
    public Iterator<String> getFilesList() {
        return new ChunkFilteredIterator(super.getFilesList());
    }

}
