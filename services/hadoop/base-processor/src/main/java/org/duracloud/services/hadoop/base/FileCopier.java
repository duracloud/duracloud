/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;

import java.io.File;
import java.io.IOException;

/**
 * This class can be run as a separate thread to copy a file either from the
 * local filesystem to a remote path, or from a remote path to the local
 * filesystem.
 *
 * @author Andrew Woods
 *         Date: Sep 24, 2010
 */
public class FileCopier implements Runnable {

    public static final String LOCAL_FS = "file://";

    private File localFile;
    private Path remotePath;
    private boolean toLocal;

    public FileCopier(File localFile, Path remotePath, boolean toLocal) {
        this.localFile = localFile;
        this.remotePath = remotePath;
        this.toLocal = toLocal;
    }

    @Override
    public void run() {
        try {
            if (toLocal) {
                copyFileToLocal();

            } else {
                copyFileFromLocal();
            }

        } catch (IOException e) {
            log("Error copying file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void copyFileToLocal() throws IOException {
        String fileName = remotePath.getName();
        FileSystem fs = remotePath.getFileSystem(new JobConf());

        if (fs.isFile(remotePath)) {
            log("Copying file (" + fileName + ") to local file system");

            Path localPath = new Path(LOCAL_FS + localFile.getAbsolutePath());
            fs.copyToLocalFile(remotePath, localPath);

            if (localFile.exists()) {
                log("File moved to local storage successfully.");

            } else {
                StringBuilder sb = new StringBuilder("Failure ");
                sb.append("attempting to move remote file (");
                sb.append(fileName);
                sb.append(") to local filesystem; local file (");
                sb.append(localFile.getAbsolutePath());
                sb.append(") not found after transfer.");
                log(sb.toString());
                throw new IOException(sb.toString());
            }

        } else {
            StringBuilder sb = new StringBuilder("Failure ");
            sb.append("attempting to access remote file (");
            sb.append(fileName);
            sb.append("), the file could not be found");
            log(sb.toString());
            throw new IOException(sb.toString());
        }
    }

    private void copyFileFromLocal() throws IOException {
        Path localPath = new Path(LOCAL_FS + localFile.getAbsolutePath());

        StringBuilder sb = new StringBuilder("Moving file: ");
        sb.append(localPath.toString());
        sb.append(" to output ");
        sb.append(remotePath.toString());
        log(sb.toString());

        FileSystem outputFS = remotePath.getFileSystem(new JobConf());
        outputFS.moveFromLocalFile(localPath, remotePath);
    }

    private void log(String msg) {
        System.out.println(msg);
    }

}
