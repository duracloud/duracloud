/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import static java.text.MessageFormat.format;
import static org.duracloud.chunk.manifest.ChunksManifest.manifestSuffix;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.duracloud.chunk.FileChunker;
import org.duracloud.chunk.FileChunkerOptions;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.ChunksManifestBean;
import org.duracloud.chunk.util.ChunksManifestVerifier;
import org.duracloud.chunk.writer.DuracloudContentWriter;
import org.duracloud.client.ContentStore;
import org.duracloud.common.retry.Retrier;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.stitch.FileStitcher;
import org.duracloud.stitch.datasource.impl.DuraStoreDataSource;
import org.duracloud.stitch.impl.FileStitcherImpl;
import org.duracloud.sync.config.SyncToolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Bill Branan
 * Date: Apr 8, 2010
 */
public class DuraStoreChunkSyncEndpoint extends DuraStoreSyncEndpoint {

    private final Logger log = LoggerFactory.getLogger(
        DuraStoreChunkSyncEndpoint.class);

    private FileStitcher stitcher;

    private boolean jumpStart;
    private FileChunkerOptions chunkerOptions;

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

        if (maxFileSize % 1000 != 0) {
            throw new RuntimeException("Max file size must be factor of 1000");
        }

        this.jumpStart = jumpStart;
        this.chunkerOptions = new FileChunkerOptions(maxFileSize);

        stitcher = new FileStitcherImpl(new DuraStoreDataSource(contentStore));
    }

    @Override
    protected Map<String, String> getContentProperties(String spaceId,
                                                       String contentId) {
        Map<String, String> props = super.getContentProperties(spaceId,
                                                               contentId);

        if (null == props) {
            try {
                ChunksManifest manifest = this.stitcher.getManifest(spaceId, getManifestId(contentId));

                if (chunksInDuraCloudMatchChunksInManifest(spaceId, manifest)) {
                    props = getManifestProperties(spaceId, manifest);
                }

            } catch (Exception ex) {
                log.debug("Not a chunked content item: {}/{}", spaceId, contentId);
            }

        }

        return props;
    }

    private boolean chunksInDuraCloudMatchChunksInManifest(String spaceId,
                                                           ChunksManifest manifest) {
        try {
            ChunksManifestVerifier verifier =
                new ChunksManifestVerifier(getContentStore());
            return verifier.verifyAllChunks(spaceId, manifest).isSuccess();
        } catch (Exception e) {
            log.warn("chunked file does not exist or is not valid: {}/{}",
                     spaceId,
                     manifest.getManifestId());
            return false;
        }
    }

    private Map<String, String> getManifestProperties(String spaceId,
                                                      ChunksManifest manifest) {
        Map<String, String> props = null;
        String manifestId = manifest.getManifestId();
        try {

            Content manifesContentItem = stitcher.getContentFromManifest(spaceId,
                                                                         manifestId);
            props = manifesContentItem.getProperties();
            log.info("Manifest found for content: {}/{}", spaceId, manifestId);

        } catch (Exception e) {
            log.debug("Not a chunked content item: {}/{}", spaceId, manifestId);
        }

        return props;
    }

    protected String getManifestId(String contentId) {
        String manifestId = contentId + manifestSuffix;
        return manifestId;
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
        String manifestId = getManifestId(contentId);
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
        Map<String, String> properties = createProps(syncFile.getAbsolutePath(), getUsername());
        final ContentStore store = getContentStore();

        DuracloudContentWriter contentWriter =
            new DuracloudContentWriter(store, getUsername(), true, this.jumpStart);
        FileChunker chunker = new FileChunker(contentWriter, chunkerOptions);
        final String spaceId = getSpaceId();
        chunker.addContent(spaceId,
                           contentId,
                           syncFile.getChecksum(),
                           syncFile.length(),
                           syncFile.getStream(),
                           properties);

        cleanup(contentId, syncFile, store, spaceId);
    }

    private void cleanup(final String contentId,
                         final MonitoredFile syncFile,
                         final ContentStore store,
                         final String spaceId) {
        //clean up any orphaned chunks and / or obsolete unchunked files
        try {
            new Retrier().execute(() -> {
                log.debug("checking for chunks, manifests, and/or unchunked files that should be removed : for {}/{}",
                          spaceId, contentId);
                Iterator<String> chunkedContentIdIt = store.getSpaceContents(getSpaceId(), contentId + ".dura-");
                Set<String> chunkedContentIds = new HashSet<>();
                chunkedContentIdIt.forEachRemaining(id -> chunkedContentIds.add(id));
                //if there are chunks
                if (!chunkedContentIds.isEmpty()) {
                    //clean up
                    if (syncFile.length() <= chunkerOptions.getMaxChunkSize()) {
                        log.info("A chunked version was replaced by an unchunked version of {}/{}",
                                 spaceId, contentId);

                        //if file is less than or equal to max chunk size
                        //then we can expect the new file is unchunked
                        //look for any chunked content matching path
                        //delete all.
                        chunkedContentIds.stream().forEach(content -> {
                            deleteContent(spaceId, content, store);
                        });
                        log.info("Deleted manifest and all chunks associated with {}/{} " +
                                 "because the chunked file was replaced by an unchunked file with " +
                                 "the same name.",
                                 spaceId, contentId);
                    } else {
                        log.debug("Checking for orphaned chunks associated with {}/{}",
                                  spaceId, contentId);

                        //resolve the set of the chunks in the manifest
                        ChunksManifest manifest = getManifest(spaceId, contentId);
                        Set<String> manifestChunks = new HashSet<>();
                        manifest.getEntries().stream().forEach(entry -> manifestChunks.add(entry.getChunkId()));
                        //for each chunk in storage, delete if not in the manifest.
                        chunkedContentIds.stream()
                                         .filter(chunkedContentId -> !chunkedContentId.endsWith(manifestSuffix))
                                         .forEach(chunk -> {
                                             if (!manifestChunks.contains(chunk)) {
                                                 log.debug("Chunk not found in manifest: deleting orphaned chunk ({}/{})",
                                                           spaceId, chunk);
                                                 deleteContent(spaceId, chunk, store);
                                             }
                                         });
                        //check for an unchunked version and remove it if happens to exist.
                        if (store.contentExists(spaceId, contentId)) {
                            deleteContent(spaceId, contentId, store);
                        }
                    }
                }

                return null;
            });
        } catch (Exception ex) {
            log.error(format("Cleanup failed for  ({0}/{1})",
                             spaceId, contentId), ex);
        }
    }

    private void deleteContent(String spaceId, String contentId, ContentStore store) {
        try {
            store.deleteContent(spaceId, contentId);
            log.debug("Deleted content  ({}/{})", spaceId, contentId);
        } catch (Exception ex) {
            final String message = format("Failed to delete content ({0}/{1}) due to {2}." +
                                          " As this is a non-critical failure, processing will " +
                                          "continue on.", spaceId, contentId, ex.getMessage());
            log.error(message, ex);
        }
    }

    @Override
    public Iterator<String> getFilesList() {
        return new ChunkFilteredIterator(super.getFilesList());
    }

}
