/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.upload;

/**
 * @author: Bill Branan
 * Date: 10/19/11
 */
public class UploadStatus {

    private boolean complete;
    private long totalFiles;
    private long completeFiles;

    public UploadStatus(boolean complete, long totalFiles, long completeFiles) {
        this.totalFiles = totalFiles;
        this.completeFiles = completeFiles;
    }

    public boolean isComplete() {
        return complete;
    }

    public long getTotalFiles() {
        return totalFiles;
    }

    public long getCompleteFiles() {
        return completeFiles;
    }

}
