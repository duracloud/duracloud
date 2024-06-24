/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.chunk;

import static java.text.MessageFormat.format;
import static org.duracloud.chunk.manifest.ChunksManifest.manifestSuffix;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.duracloud.chunk.ChunkableContent;
import org.duracloud.chunk.FileChunkerOptions;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.ChunksManifestBean;
import org.duracloud.chunk.stream.ChunkInputStream;
import org.duracloud.chunk.util.ChunkUtil;
import org.duracloud.chunk.writer.AddContentResult;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreImpl;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.domain.StorageProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ContentStore which can chunk files larger than the maximum file size
 *
 * @author mikejritter
 */
public class ChunkingContentStoreImpl extends ContentStoreImpl {
    private static final Logger log = LoggerFactory.getLogger(ChunkingContentStoreImpl.class);

    private final FileChunkerOptions options;
    private final ChecksumUtil checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);

    /**
     * Create a ChunkingContentStoreImpl with default FileChunkerOptions
     *
     * @param baseURL the baseUrl of the content store
     * @param type the StorageProviderType
     * @param storeId The spaceId
     * @param writable whether the store is writable
     * @param restHelper the RestHttpHelper
     * @param maxRetries the maximum number of retries
     */
    public ChunkingContentStoreImpl(final String baseURL,
                                    final StorageProviderType type,
                                    final String storeId,
                                    final boolean writable,
                                    final RestHttpHelper restHelper,
                                    final int maxRetries) {
        this(baseURL, type, storeId, writable, restHelper, maxRetries, new FileChunkerOptions());
    }

    public ChunkingContentStoreImpl(final String baseURL,
                                    final StorageProviderType type,
                                    final String storeId,
                                    final boolean writable,
                                    final RestHttpHelper restHelper,
                                    final int maxRetries,
                                    final FileChunkerOptions options) {
        super(baseURL, type, storeId, writable, restHelper, maxRetries);
        this.options = options;
    }

    /**
     * todo: maybe for chunked content what we do is have this as a separate class (e.g. ChunkHandler) and move
     * addContent and doAddContent there from ContentStoreImpl. Just an idea.
     *
     * {@inheritDoc}
     */
    @Override
    public String addContent(final String spaceId,
                             final String contentId,
                             final InputStream content,
                             final long contentSize,
                             final String contentMimeType,
                             final String contentChecksum,
                             final Map<String, String> contentProperties)
        throws ContentStoreException {

        final var maxChunkSize = options.getMaxChunkSize();
        if (contentSize <= maxChunkSize) {
            final var md5 = super.addContent(spaceId,
                                             contentId,
                                             content,
                                             contentSize,
                                             contentMimeType,
                                             contentChecksum,
                                             contentProperties);
            cleanupChunkedContent(spaceId, contentId);
            return md5;
        } else {
            return chunkContent(spaceId, contentId, content, contentSize, contentChecksum, contentProperties);
        }
    }

    private String chunkContent(final String spaceId,
                                final String contentId,
                                final InputStream content,
                                final long contentSize,
                                final String contentChecksum,
                                final Map<String, String> contentProperties) throws ContentStoreException {
        // todo: do we want to keep these boolean values? should they be default true for the client?
        final var maxChunkSize = options.getMaxChunkSize();
        final var ignoreLargeFiles = options.isIgnoreLargeFiles();
        final var preserveChunkMD5s = options.isPreserveChunkMD5s();

        if (!ignoreLargeFiles) {
            final var chunkedContent = new ChunkableContent(contentId, content, contentSize, maxChunkSize);
            chunkedContent.setPreserveChunkMD5s(preserveChunkMD5s);
            final var results = addChunkableContent(spaceId, chunkedContent, contentProperties);
            final var manifest = addChunkManifest(chunkedContent, spaceId, contentProperties, results);

            // before or after finalChecksum verify?
            cleanupOrphanedChunks(spaceId, contentId, manifest);

            // Verify final checksum
            String finalChecksum = "";
            if (contentChecksum != null) {
                finalChecksum = chunkedContent.getManifest().getHeader().getSourceMD5();
                if (!contentChecksum.equals(finalChecksum)) {
                    String err = "Final checksum of chunked content " + finalChecksum +
                                 " does not match provided checksum " + contentChecksum;
                    throw new DuraCloudRuntimeException(err);
                }
            }

            return finalChecksum;
        } else {
            // todo: anything to do for ignored files? or do we want to autochunk?
            log.info("Ignoring: [{}] (file too large)", contentId);
        }

        return "";
    }

    public List<AddContentResult> addChunkableContent(final String spaceId,
                                                      final ChunkableContent content,
                                                      final Map<String, String> contentProperties) {
        final var results = new ArrayList<AddContentResult>();

        // todo: how does this work if there's an error midway?
        for (final ChunkInputStream chunk: content) {
            final var chunkId = chunk.getChunkId();
            final var chunkSize = chunk.getChunkSize();
            // todo: this causes the progress of the synctool to increase by the % of the chunk
            // is there a better way to handle it?
            final var chunkFile = IOUtil.writeStreamToFile(chunk);

            try {
                final var result = new AddContentResult(spaceId, chunkId, chunkSize);
                final var chunkChecksum = getChunkChecksum(chunkFile);

                // Write chunk if it is not already in storage
                if (!chunkInStorage(spaceId, chunkId, chunkChecksum)) {
                    // similar to addContent in ContentStoreImpl, don't retry
                    try (InputStream chunkStream = new FileInputStream(chunkFile)) {
                        ChunkInputStream chunkFileStream =
                            new ChunkInputStream(chunkId,
                                                 chunkStream,
                                                 chunkFile.length(),
                                                 chunk.md5Preserved());
                        final var md5 = super.addContent(spaceId,
                                         chunkId,
                                         chunkFileStream,
                                         chunkFileStream.getChunkSize(),
                                         chunkFileStream.getMimetype(),
                                         chunkChecksum,
                                         contentProperties);

                        if (md5 != null) {
                            result.setMd5(md5);
                            result.setState(AddContentResult.State.SUCCESS);
                        }
                        results.add(result);
                    } catch (IOException | ContentStoreException e) {
                        String err = "Failed to store chunk with ID " + chunkId +
                                     " in space " + spaceId + ". Last error: " + e.getMessage();
                        throw new DuraCloudRuntimeException(err, e);
                    }
                }
            } finally {
                if (null != chunkFile && chunkFile.exists()) {
                    FileUtils.deleteQuietly(chunkFile);
                }
            }
        }

        return results;
    }

    private ChunksManifest addChunkManifest(final ChunkableContent chunkedContent,
                                            final String spaceId,
                                            final Map<String, String> contentProperties,
                                            final List<AddContentResult> results) {
        final var manifest = chunkedContent.finalizeManifest();
        final var success = results.stream()
            .allMatch(result -> result.getState() == AddContentResult.State.SUCCESS);

        if (success) {
            final var manifestId = manifest.getManifestId();
            try (final var manifestBody = manifest.getBody()) {
                final var manifestChecksum = checksumUtil.generateChecksum(manifest.getBody());
                super.addContent(spaceId,
                                 manifestId,
                                 manifestBody,
                                 manifestBody.getLength(),
                                 manifest.getMimetype(),
                                 manifestChecksum,
                                 contentProperties);
            } catch (IOException | ContentStoreException e) {
                final var err = "Failed to add manifest " + manifestId + ": " + e.getMessage();
                throw new DuraCloudRuntimeException(err, e);
            }

            // todo: move to cleanup?
            //check if an unchunked version of the file exists and, if so delete it.
            final var contentId = new ChunkUtil().preChunkedContentId(manifestId);
            try {
                if (contentExists(spaceId, contentId)) {
                    deleteContent(spaceId, contentId);
                }
            } catch (ContentStoreException e) {
                log.warn("Failed to delete formerly unchunked content item {} in space {}.", contentId, spaceId, e);
            }
        }

        log.debug("wrote: {}, {}", spaceId, manifest.getManifestId());
        return manifest;
    }

    /*
     * Determine the checksum of the chunk file
     */
    private String getChunkChecksum(File chunkFile) {
        try {
            return checksumUtil.generateChecksum(chunkFile);
        } catch (IOException e) {
            throw new DuraCloudRuntimeException("Unable to generate checksum for chunk " +
                                                chunkFile + " due to: " + e.getMessage());
        }
    }

    /*
     * Determines if a file chunk exists in DuraCloud storage with the given checksum
     */
    private boolean chunkInStorage(String spaceId, String contentId, String checksum) {
        try {
            if (contentExists(spaceId, contentId)) { // dc file exists
                var props = getContentProperties(spaceId, contentId);
                final var dcChecksum = props.get(ContentStore.CONTENT_CHECKSUM);
                // File exists in DuraCloud and checksums match
                return null != checksum && checksum.equals(dcChecksum);
            } else {
                return false; // File does not exist in DuraCloud
            }
        } catch (ContentStoreException e) {
            return false; // File does not exist in DuraCloud
        }
    }

    private void cleanupChunkedContent(final String spaceId, final String contentId) throws ContentStoreException {
        final var chunkedContentIdIt = getSpaceContents(spaceId, contentId + ".dura-");
        if (chunkedContentIdIt.hasNext()) {
            log.info("A chunked version was replaced by an unchunked version of {}/{}", spaceId, contentId);
            chunkedContentIdIt.forEachRemaining(chunkId -> {
                tryDeleteContent(spaceId, chunkId);
            });
            log.info("Deleted manifest and all chunks associated with {}/{} " +
                     "because the chunked file was replaced by an unchunked file with " +
                     "the same name.",
                     spaceId, contentId);
        }
    }

    private void cleanupOrphanedChunks(final String spaceId,
                                       final String contentId,
                                       final ChunksManifest manifest) throws ContentStoreException {
        log.debug("Checking for orphaned chunks associated with {}/{}", spaceId, contentId);

        // resolve the set of the chunks in the manifest
        final var manifestChunks = manifest.getEntries().stream()
                                           .map(ChunksManifestBean.ManifestEntry::getChunkId)
                                           .collect(Collectors.toSet());

        // read the chunked content IDs from duracloud and remove chunks not contained in the manifest
        final var chunkedContentIdIt = getSpaceContents(spaceId, contentId + ".dura-");
        while (chunkedContentIdIt.hasNext()) {
            final var chunk = chunkedContentIdIt.next();
            if (!chunk.endsWith(manifestSuffix) && !manifestChunks.contains(chunk)) {
                log.debug("Chunk not found in manifest: deleting orphaned chunk ({}/{})", spaceId, chunk);
                tryDeleteContent(spaceId, chunk);
            }
        }
    }

    private void tryDeleteContent(String spaceId, String contentId) {
        try {
            deleteContent(spaceId, contentId);
            log.debug("Deleted content ({}/{})", spaceId, contentId);
        } catch (Exception ex) {
            final String message = format("Failed to delete content ({0}/{1}) due to {2}." +
                                          " As this is a non-critical failure, processing will " +
                                          "continue on.", spaceId, contentId, ex.getMessage());
            log.error(message, ex);
        }
    }

}
