/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.source;

import java.util.List;

import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.util.ChunkUtil;
import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.ContentItem;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.retrieval.mgmt.RetrievalListener;
import org.duracloud.stitch.FileStitcher;
import org.duracloud.stitch.FileStitcherListener;
import org.duracloud.stitch.datasource.impl.DuraStoreDataSource;
import org.duracloud.stitch.error.DataSourceException;
import org.duracloud.stitch.error.InvalidManifestException;
import org.duracloud.stitch.error.MissingContentException;
import org.duracloud.stitch.impl.FileStitcherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the RetrievalSource interface with support for handling
 * content that resides in DuraStore as chunks.
 *
 * @author Andrew Woods
 * Date: 9/5/11
 */
public class DuraStoreStitchingRetrievalSource extends DuraStoreRetrievalSource {

    private final Logger log = LoggerFactory.getLogger(
        DuraStoreStitchingRetrievalSource.class);

    private FileStitcher stitcher;
    private ChunkUtil chunkUtil;

    public DuraStoreStitchingRetrievalSource(ContentStore store,
                                             List<String> spaces,
                                             boolean allSpaces) {
        super(store, spaces, allSpaces);
        this.stitcher = new FileStitcherImpl(new DuraStoreDataSource(store));
        this.chunkUtil = new ChunkUtil();
    }

    @Override
    public synchronized ContentItem getNextContentItem() {
        log.debug("enter getNextContentItem()");
        ContentItem item = super.getNextContentItem();

        // skip chunks.
        if (null != item && chunkUtil.isChunk(item.getContentId())) {
            log.debug("skipping chunk item: {}", item);
            item = getNextContentItem();
        }

        log.debug("returning item: {}", item);
        return item;
    }

    @Override
    public String getSourceChecksum(ContentItem contentItem) {
        if (chunkUtil.isChunkManifest(contentItem.getContentId())) {
            ChunksManifest manifest;
            try {
                manifest = stitcher.getManifest(contentItem.getSpaceId(),
                                                contentItem.getContentId());
                return manifest.getHeader().getSourceMD5();
            } catch (Exception e) {
                throw new RuntimeException(
                    "Unable to get checksum for " + contentItem.toString() +
                    " due to: " + e.getMessage());
            }

        } else {
            return super.getSourceChecksum(contentItem);
        }
    }

    @Override
    protected Content doGetContent(ContentItem item, RetrievalListener listener) {
        log.debug("enter doGetContent: {}", item);

        if (null != item && chunkUtil.isChunk(item.getContentId())) {
            StringBuilder msg = new StringBuilder();
            msg.append("Unexpected content item: ");
            msg.append(item);
            msg.append(", chunks not allowed for direct access.");

            log.error(msg.toString());
            throw new DuraCloudRuntimeException(msg.toString());
        }

        if (null != item && chunkUtil.isChunkManifest(item.getContentId())) {
            log.debug("retrieving manifest: {}", item);
            return doGetContentFromManifest(item, listener);

        } else {
            log.debug("retrieving basic content: {}", item);
            return super.doGetContent(item, listener);
        }
    }

    protected Content doGetContentFromManifest(ContentItem item, RetrievalListener listener) {
        try {
            FileStitcherListener fileStitcherListener = new FileStitcherListener() {
                public void chunkStitched(String chunkId) {
                    if (listener != null) {
                        listener.chunkRetrieved(chunkId);
                    }
                }
            };

            return stitcher.getContentFromManifest(item.getSpaceId(),
                                                   item.getContentId(),
                                                   fileStitcherListener);
        } catch (DataSourceException dse) {
            try {
                if (contentStore.contentExists(item.getSpaceId(), item.getContentId())) {
                    throw dse;
                } else {
                    StringBuilder msg = new StringBuilder();
                    msg.append("The item does not exist in the space: ");
                    msg.append(item);
                    msg.append(dse.getMessage());

                    throw new MissingContentException(msg.toString(), dse);
                }
            } catch (ContentStoreException e) {
                throw dse;
            }
        } catch (InvalidManifestException e) {
            StringBuilder msg = new StringBuilder();
            msg.append("Unable to get content for ");
            msg.append(item);
            msg.append(" due to: ");
            msg.append(e.getMessage());

            log.error(msg.toString());
            throw new RuntimeException(msg.toString());
        }
    }

}
