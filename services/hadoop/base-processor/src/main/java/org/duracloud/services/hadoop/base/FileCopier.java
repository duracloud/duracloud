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
import org.apache.hadoop.fs.s3.S3Credentials;
import org.apache.hadoop.mapred.JobConf;
import org.duracloud.services.hadoop.store.MD5Util;
import org.duracloud.services.hadoop.store.UriPathUtil;

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

    public static final String LOCAL_FS = "file:///";

    private File localFile;
    private Path remotePath;
    private boolean toLocal;

    private String md5;

    private static final UriPathUtil pathUtil = new UriPathUtil();
    private static final MD5Util md5Util = new MD5Util();

    private boolean terminate;

    public FileCopier(File localFile,
                      Path remotePath,
                      boolean toLocal) {
        this.localFile = localFile;
        this.remotePath = remotePath;
        this.toLocal = toLocal;
        this.terminate = false;
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

    public void terminate() {
        this.terminate = true;
    }

    private void copyFileToLocal() throws IOException {
        int tries = 0;
        int maxTries = 5;
        boolean md5Valid = false;
        while (!md5Valid && tries++ < maxTries && !terminate) {
            try {
                doCopyFileToLocal();
                String md5Local = getMd5FromLocal();
                String md5Remote = getMd5FromProperties();
                if (md5Local.equals(md5Remote)) {
                    md5Valid = true;
                    setMd5(md5Local);
                    log("MD5s match for: " + remotePath + ": " + md5Remote);

                } else {
                    StringBuilder sb = new StringBuilder("md5 mismatch [");
                    sb.append(remotePath);
                    sb.append(":");
                    sb.append(md5Remote);
                    sb.append("], [");
                    sb.append(localFile.getAbsolutePath());
                    sb.append(":");
                    sb.append(md5Local);
                    sb.append("] download again.");
                    log(sb.toString());
                }
            } catch (IOException e) {
                log("Error copying: " + remotePath + ", " + e.getMessage());
            }
        }

        if (!md5Valid) {
            throw new IOException("Unable to download valid: " + remotePath);
        }
    }

    private String getMd5FromLocal() {
        return md5Util.getMd5(localFile);
    }

    protected String getMd5FromProperties() throws IOException {
        JobConf job = new JobConf();
        FileSystem fs = remotePath.getFileSystem(job);
        S3Credentials s3Credentials = new S3Credentials();
        s3Credentials.initialize(fs.getUri(), job);

        String bucketId = pathUtil.getBucketId(remotePath.toString());
        String contentId = pathUtil.getContentId(remotePath.toString());
        return md5Util.getMd5(s3Credentials, bucketId, contentId);
    }

    private void doCopyFileToLocal() throws IOException {
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
        outputFS.copyFromLocalFile(localPath, remotePath);
    }

    public String getMd5() {
        return md5;
    }

    private void setMd5(String md5) {
        this.md5 = md5;
    }

    private void log(String msg) {
        System.out.println(msg);
    }

}