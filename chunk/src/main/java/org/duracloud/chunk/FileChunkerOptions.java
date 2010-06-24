/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * This class is a simple container for configuration options for the
 * FileChunker.
 *
 * @author Andrew Woods
 *         Date: Mar 17, 2010
 */
public class FileChunkerOptions {
    private IOFileFilter fileFilter = TrueFileFilter.TRUE;
    private IOFileFilter dirFilter = TrueFileFilter.TRUE;
    private long maxChunkSize = 1073741824; // 1-GB
    private boolean preserveChunkMD5s = true;
    private boolean ignoreLargeFiles = false;

    public FileChunkerOptions() {
        // use defaults.
    }

    public FileChunkerOptions(long maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }

    public FileChunkerOptions(IOFileFilter dirFilter, long maxChunkSize) {
        this.dirFilter = dirFilter;
        this.maxChunkSize = maxChunkSize;
    }

    public FileChunkerOptions(long maxChunkSize, boolean ignoreLargeFiles) {
        this.maxChunkSize = maxChunkSize;
        this.ignoreLargeFiles = ignoreLargeFiles;
    }

    public FileChunkerOptions(IOFileFilter fileFilter,
                              IOFileFilter dirFilter,
                              long maxChunkSize) {
        this.fileFilter = fileFilter;
        this.dirFilter = dirFilter;
        this.maxChunkSize = maxChunkSize;
    }

    public FileChunkerOptions(IOFileFilter fileFilter,
                              IOFileFilter dirFilter,
                              long maxChunkSize,
                              boolean preserveChunkMD5s,
                              boolean ignoreLargeFiles) {
        this.fileFilter = fileFilter;
        this.dirFilter = dirFilter;
        this.maxChunkSize = maxChunkSize;
        this.preserveChunkMD5s = preserveChunkMD5s;
        this.ignoreLargeFiles = ignoreLargeFiles;
    }

    public IOFileFilter getFileFilter() {
        return fileFilter;
    }

    public IOFileFilter getDirFilter() {
        return dirFilter;
    }

    public long getMaxChunkSize() {
        return maxChunkSize;
    }

    public boolean isPreserveChunkMD5s() {
        return preserveChunkMD5s;
    }

    public boolean isIgnoreLargeFiles() {
        return ignoreLargeFiles;
    }
}
