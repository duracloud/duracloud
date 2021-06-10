/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch.impl;

import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_CHECKSUM;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_MD5;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_SIZE;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.ChunksManifestBean;
import org.duracloud.chunk.manifest.xml.ManifestDocumentBinding;
import org.duracloud.common.model.ContentItem;
import org.duracloud.domain.Content;
import org.duracloud.stitch.FileStitcher;
import org.duracloud.stitch.FileStitcherListener;
import org.duracloud.stitch.datasource.DataSource;
import org.duracloud.stitch.error.DataSourceException;
import org.duracloud.stitch.error.InvalidManifestException;
import org.duracloud.stitch.stream.MultiContentInputStream;
import org.duracloud.stitch.stream.MultiContentInputStreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the FileStitcher interface.
 *
 * @author Andrew Woods
 * Date: 9/2/11
 */
public class FileStitcherImpl implements FileStitcher {

    private Logger log = LoggerFactory.getLogger(FileStitcherImpl.class);

    private DataSource dataSource;

    public FileStitcherImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Content getContentFromManifest(String spaceId, String contentId, FileStitcherListener listener)
        throws InvalidManifestException, DataSourceException {
        log.debug("getContentFromManifest({}, {})", spaceId, contentId);

        // verify contentId corresponds to the manifest naming convention.
        if (!isManifest(contentId)) {
            log.error("Invalid manifest name: {}", contentId);
            throw new InvalidManifestException(spaceId, contentId);
        }

        Content manifestContent = this.dataSource.getContent(spaceId, contentId);
        // get deserialized manifest.
        ChunksManifest manifest = getManifest(manifestContent, spaceId, contentId);

        // collect ordered sequence of chunk streams.
        InputStream multiStream = getChunkSequenceStream(spaceId, manifest, listener);

        // package the chunks as the reconstituted content item.
        Content content = new Content();
        content.setStream(multiStream);
        content.setId(manifest.getHeader().getSourceContentId());

        //merge properties by overlaying stitched props over manifest props
        Map<String, String> stitchedProps = getContentProperties(manifest);
        Map<String, String> manifestProps = manifestContent.getProperties();
        if (manifestProps == null) {
            manifestProps = stitchedProps;
        } else {
            manifestProps.putAll(stitchedProps);
        }
        content.setProperties(manifestProps);
        return content;
    }

    private boolean isManifest(String contentId) {
        return null != contentId &&
               contentId.endsWith(ChunksManifest.manifestSuffix);
    }

    @Override
    public ChunksManifest getManifest(String spaceId, String manifestId)
        throws InvalidManifestException {
        return getManifest(dataSource.getContent(spaceId, manifestId), spaceId, manifestId);
    }

    private ChunksManifest getManifest(Content content, String spaceId, String manifestId)
        throws InvalidManifestException {
        if (null == content) {
            String msg = "No content found!";
            log.error(msg);
            throw new InvalidManifestException(spaceId, manifestId, msg);
        }

        try (InputStream is = content.getStream()) {
            return ManifestDocumentBinding.createManifestFrom(is);

        } catch (Exception e) {
            String msg = "Error deserializing manifest!";
            log.error(msg);
            throw new InvalidManifestException(spaceId, manifestId, msg, e);
        }
    }

    private InputStream getChunkSequenceStream(String spaceId,
                                               ChunksManifest manifest, FileStitcherListener listener)
        throws InvalidManifestException {
        // sort chunks by their index.
        Map<Integer, String> sortedChunkIds = new TreeMap<Integer, String>();
        for (ChunksManifestBean.ManifestEntry entry : manifest.getEntries()) {
            int parsedIndex = manifest.parseIndex(entry.getChunkId());

            if (entry.getIndex() == parsedIndex) {
                sortedChunkIds.put(entry.getIndex(), entry.getChunkId());
            } else {
                log.info("The entry in the chunk manifest for chunk {} is missing an index field; using the index" +
                         " from the filename instead.", entry.getChunkId());
                sortedChunkIds.put(parsedIndex, entry.getChunkId());
            }
        }

        // collect ordered sequence of chunk streams.
        List<ContentItem> chunks = new ArrayList<ContentItem>();
        for (String chunkId : sortedChunkIds.values()) {
            chunks.add(new ContentItem(spaceId, chunkId));
        }

        if (chunks.size() == 0) {
            String msg = "No chunk streams found!";
            log.error(msg);
            String contentId = manifest.getHeader().getSourceContentId();
            throw new InvalidManifestException(spaceId, contentId, msg);
        }

        MultiContentInputStreamListener contentListener =
            new MultiContentInputStreamListener() {
                public void contentIdRead(String contentId) {
                    if (listener != null) {
                        listener.chunkStitched(contentId);
                    }
                }
            };

        return new MultiContentInputStream(dataSource, chunks, contentListener);
    }

    private Map<String, String> getContentProperties(ChunksManifest manifest) {
        Map<String, String> props = new HashMap<>();
        ChunksManifestBean.ManifestHeader header = manifest.getHeader();
        String contentSize = Long.toString(header.getSourceByteSize());
        props.put(PROPERTIES_CONTENT_SIZE, contentSize);
        props.put(PROPERTIES_CONTENT_MIMETYPE, header.getSourceMimetype());
        props.put(PROPERTIES_CONTENT_MD5, header.getSourceMD5());
        props.put(PROPERTIES_CONTENT_CHECKSUM, header.getSourceMD5());
        return props;
    }

}
