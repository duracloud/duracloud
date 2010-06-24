/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.manifest;

import java.util.List;

/**
 * This class is a simple bean for a ChunksManifest.
 *
 * @author Andrew Woods
 *         Date: Feb 9, 2010
 */
public class ChunksManifestBean {

    private ManifestHeader header;
    private List<ManifestEntry> entries;

    public ManifestHeader getHeader() {
        return header;
    }

    public void setHeader(ManifestHeader header) {
        this.header = header;
    }

    public List<ManifestEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ManifestEntry> entries) {
        this.entries = entries;
    }

    /**
     * Manifest Header inner class.
     */
    public static class ManifestHeader {
        private String sourceContentId;
        private String sourceMimetype;
        private String sourceMD5;
        private long sourceByteSize;

        public ManifestHeader(String sourceContentId,
                              String sourceMimetype,
                              long sourceByteSize) {
            this.sourceContentId = sourceContentId;
            this.sourceMimetype = sourceMimetype;
            this.sourceByteSize = sourceByteSize;
        }

        public String getSourceContentId() {
            return sourceContentId;
        }

        public String getSourceMimetype() {
            return sourceMimetype;
        }

        public long getSourceByteSize() {
            return sourceByteSize;
        }

        public String getSourceMD5() {
            return sourceMD5;
        }

        public void setSourceMD5(String sourceMD5) {
            this.sourceMD5 = sourceMD5;
        }
    }

    /**
     * Manifest Entry inner class.
     */
    public static class ManifestEntry {
        private String chunkId;
        private String chunkMD5;
        private int index;
        private long byteSize;

        public ManifestEntry(String chunkId,
                             String chunkMD5,
                             int index,
                             long byteSize) {
            this.chunkId = chunkId;
            this.chunkMD5 = chunkMD5;
            this.index = index;
            this.byteSize = byteSize;
        }

        public String getChunkId() {
            return chunkId;
        }

        public String getChunkMD5() {
            return chunkMD5;
        }

        public int getIndex() {
            return index;
        }

        public long getByteSize() {
            return byteSize;
        }
    }
}
