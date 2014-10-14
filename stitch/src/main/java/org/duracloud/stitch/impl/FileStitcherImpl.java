/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch.impl;

import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.ChunksManifestBean;
import org.duracloud.chunk.manifest.xml.ManifestDocumentBinding;
import org.duracloud.common.model.ContentItem;
import org.duracloud.domain.Content;
import org.duracloud.stitch.FileStitcher;
import org.duracloud.stitch.datasource.DataSource;
import org.duracloud.stitch.error.InvalidManifestException;
import org.duracloud.stitch.stream.MultiContentInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_CHECKSUM;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_MD5;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_SIZE;

/**
 * This class implements the FileStitcher interface.
 *
 * @author Andrew Woods
 *         Date: 9/2/11
 */
public class FileStitcherImpl implements FileStitcher {

    private Logger log = LoggerFactory.getLogger(FileStitcherImpl.class);

    private DataSource dataSource;

    public FileStitcherImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Content getContentFromManifest(String spaceId, String contentId)
        throws InvalidManifestException {
        log.debug("getContentFromManifest({}, {})", spaceId, contentId);

        // verify contentId corresponds to the manifest naming convention.
        if (!isManifest(contentId)) {
            log.error("Invalid manifest name: {}", contentId);
            throw new InvalidManifestException(spaceId, contentId);
        }

        // get deserialized manifest.
        ChunksManifest manifest = getManifest(spaceId, contentId);

        // collect ordered sequence of chunk streams.
        InputStream multiStream = getChunkSequenceStream(spaceId, manifest);

        // package the chunks as the reconstituted content item.
        Content content = new Content();
        content.setStream(multiStream);
        content.setId(manifest.getHeader().getSourceContentId());
        content.setProperties(getContentProperties(manifest));

        return content;
    }

    private boolean isManifest(String contentId) {
        return null != contentId &&
            contentId.endsWith(ChunksManifest.manifestSuffix);
    }

    @Override
    public ChunksManifest getManifest(String spaceId, String manifestId)
        throws InvalidManifestException {
        Content content = dataSource.getContent(spaceId, manifestId);
        if (null == content) {
            String msg = "No content found!";
            log.error(msg);
            throw new InvalidManifestException(spaceId, manifestId, msg);
        }

        try {
            return ManifestDocumentBinding.createManifestFrom(content.getStream());

        } catch (Exception e) {
            String msg = "Error deserializing manifest!";
            log.error(msg);
            throw new InvalidManifestException(spaceId, manifestId, msg, e);
        }
    }

    private InputStream getChunkSequenceStream(String spaceId,
                                               ChunksManifest manifest)
        throws InvalidManifestException {
        // sort chunks by their index.
        Map<Integer, String> sortedChunkIds = new TreeMap<Integer, String>();
        for (ChunksManifestBean.ManifestEntry entry : manifest.getEntries()) {
            sortedChunkIds.put(entry.getIndex(), entry.getChunkId());
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

        return new MultiContentInputStream(dataSource, chunks);
    }

    private Map<String, String> getContentProperties(ChunksManifest manifest) {
        Map<String, String> props = new HashMap<String, String>();
        ChunksManifestBean.ManifestHeader header = manifest.getHeader();

        String contentSize = Long.toString(header.getSourceByteSize());
        props.put(PROPERTIES_CONTENT_SIZE, contentSize);
        props.put(PROPERTIES_CONTENT_MIMETYPE, header.getSourceMimetype());
        props.put(PROPERTIES_CONTENT_MD5, header.getSourceMD5());
        props.put(PROPERTIES_CONTENT_CHECKSUM, header.getSourceMD5());

        return props;
    }

}
