/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.util;

import org.duracloud.chunk.manifest.ChunksManifest;

/**
 * This class provides chunk and manifest contentId helpers.
 *
 * @author Andrew Woods
 * Date: 9/8/11
 */
public class ChunkUtil {

    public String preChunkedContentId(String contentId) {
        if (!isChunkManifest(contentId)) {
            return contentId;
        }

        int end = contentId.indexOf(ChunksManifest.manifestSuffix);
        return contentId.substring(0, end);
    }

    public boolean isChunkManifest(String contentId) {
        if (null == contentId) {
            return false;
        }
        return contentId.endsWith(ChunksManifest.manifestSuffix);
    }

    public boolean isChunk(String contentId) {
        if (null == contentId) {
            return false;
        }
        return contentId.matches(".*" + ChunksManifest.chunkSuffix + "\\d+");
    }

}
