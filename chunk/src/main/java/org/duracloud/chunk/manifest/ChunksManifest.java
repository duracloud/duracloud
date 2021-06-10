/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.manifest;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.duracloud.chunk.manifest.xml.ManifestDocumentBinding;
import org.duracloud.chunk.stream.KnownLengthInputStream;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrew Woods
 * Date: Feb 7, 2010
 */
public class ChunksManifest extends ChunksManifestBean {

    private final Logger log = LoggerFactory.getLogger(ChunksManifest.class);

    public static final String SCHEMA_VERSION = "0.2";

    private int chunkIndex = -1;
    private final static String mimetype = "application/xml";
    public final static String chunkSuffix = ".dura-chunk-";
    public final static String manifestSuffix = ".dura-manifest";
    private static final int MAX_CHUNKS = 9999;

    public ChunksManifest(ChunksManifestBean bean) {
        this.setEntries(bean.getEntries());
        this.setHeader(bean.getHeader());
    }

    public ChunksManifest(String sourceContentId,
                          String sourceMimetype,
                          long sourceByteSize) {
        this.setEntries(new ArrayList<ManifestEntry>());
        this.setHeader(new ManifestHeader(sourceContentId,
                                          sourceMimetype,
                                          sourceByteSize));
    }

    public void setMD5OfSourceContent(String md5) {
        getHeader().setSourceMD5(md5);
    }

    public String getManifestId() {
        return getHeader().getSourceContentId() + manifestSuffix;
    }

    public String nextChunkId() {
        if (chunkIndex >= MAX_CHUNKS) {
            throw new DuraCloudRuntimeException("Max chunks: " + MAX_CHUNKS);
        }
        return getHeader().getSourceContentId() + chunkSuffix + nextChunkIndex();
    }

    private String nextChunkIndex() {
        return String.format("%1$04d", ++chunkIndex);
    }

    public void addEntry(String chunkId, String chunkMD5, long chunkSize) {
        getEntries().add(new ManifestEntry(chunkId,
                                           chunkMD5,
                                           parseIndex(chunkId),
                                           chunkSize));
    }

    public int parseIndex(String chunkId) {
        String prefix = getHeader().getSourceContentId() + chunkSuffix;
        String num = chunkId.substring(prefix.length());
        try {
            return Integer.parseInt(num);

        } catch (NumberFormatException e) {
            StringBuilder msg = new StringBuilder("ChunkId's must be");
            msg.append("formatted with trailing index: " + chunkId + ", ");
            msg.append(e.getMessage());
            log.error(msg.toString(), e);
            throw new DuraCloudRuntimeException(msg.toString(), e);
        }
    }

    public KnownLengthInputStream getBody() {
        String xml = ManifestDocumentBinding.createDocumentFrom(this);
        log.debug("Manifest body: '" + xml + "'");

        try {
            return new KnownLengthInputStream(xml);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getMimetype() {
        return mimetype;
    }

}
