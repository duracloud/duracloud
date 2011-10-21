/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.upload;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: 10/19/11
 */
public class UploadStatus {

    private boolean complete;
    private int totalFiles;
    private int completeFiles;
    private List<FileInTransfer> filesInTransfer;

    public UploadStatus(boolean complete, int totalFiles, int completeFiles) {
        this.complete = complete;
        this.totalFiles = totalFiles;
        this.completeFiles = completeFiles;

        this.filesInTransfer = new ArrayList<FileInTransfer>();
    }

    public boolean isComplete() {
        return complete;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public int getCompleteFiles() {
        return completeFiles;
    }

    public List<FileInTransfer> getFilesInTransfer() {
        return filesInTransfer;
    }

    public void addFileInTransfer(String name, long totalSize, long bytesRead) {
        filesInTransfer.add(new FileInTransfer(name, totalSize, bytesRead));
    }

    public class FileInTransfer {
        private String name;
        private long totalSize;
        private long bytesRead;

        public FileInTransfer(String name, long totalSize, long bytesRead) {
            this.name = name;
            this.totalSize = totalSize;
            this.bytesRead = bytesRead;
        }

        public String getName() {
            return name;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public long getBytesRead() {
            return bytesRead;
        }
    }
}
