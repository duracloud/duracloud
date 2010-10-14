/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.hadoop.store.MimeTypeUtil;
import org.duracloud.services.hadoop.store.UriPathUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class can be run as a separate thread to copy a file either from the
 * local filesystem to a remote path, or from a remote path to the local
 * filesystem.
 *
 * @author Andrew Woods
 *         Date: Sep 24, 2010
 */
public class FileCopier implements Runnable {

    private File localFile;
    private Path remotePath;
    private boolean toLocal;
    private ContentStore store;

    private static final UriPathUtil pathUtil = new UriPathUtil();
    private static final MimeTypeUtil mimeUtil = new MimeTypeUtil();

    public FileCopier(File localFile,
                      Path remotePath,
                      boolean toLocal,
                      ContentStore store) {
        this.localFile = localFile;
        this.remotePath = remotePath;
        this.toLocal = toLocal;
        this.store = store;
    }

    @Override
    public void run() {
        try {
            if (toLocal) {
                copyFileToLocal();

            } else {
                copyFileFromLocal();
            }

        } catch (Exception e) {
            log("Error copying file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void copyFileToLocal() throws IOException {
        InputStream remoteInStream = null;
        OutputStream localOutStream = null;

        String fileName = remotePath.getName();
        FileSystem fs = remotePath.getFileSystem(new JobConf());

        if (fs.isFile(remotePath)) {
            log("Copying file (" + fileName + ") to local file system");

            try {
                remoteInStream = getRemoteContent(remotePath);
                localOutStream = FileUtils.openOutputStream(localFile);
                IOUtils.copy(remoteInStream, localOutStream);

            } catch (Exception e) {
                log("Error copying content: " + e.getMessage());

            } finally {
                IOUtils.closeQuietly(remoteInStream);
                IOUtils.closeQuietly(localOutStream);
            }

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

    private InputStream getRemoteContent(Path remotePath)
        throws ContentStoreException, IOException {

        Content content = store.getContent(getSpaceId(), getContentId());
        if (null != content && null != content.getStream()) {
            return content.getStream();

        } else {
            throw new IOException("Null content for: " + remotePath);
        }
    }

    private void copyFileFromLocal() throws IOException {
        StringBuilder sb = new StringBuilder("Moving file: ");
        sb.append(localFile.getAbsolutePath());
        sb.append(" to output ");
        sb.append(remotePath.toString());
        log(sb.toString());

        putContent(FileUtils.openInputStream(localFile));
    }

    private void putContent(InputStream localInStream) throws IOException {
        try {
            store.addContent(getSpaceId(),
                             getContentId(),
                             localInStream,
                             localFile.length(),
                             mimeUtil.guessMimeType(getContentId()),
                             null,
                             null);

        } catch (ContentStoreException e) {
            e.printStackTrace();
            throw new IOException("Error adding content: " + e.getMessage());

        } finally {
            IOUtils.closeQuietly(localInStream);
        }
    }

    private String getSpaceId() {
        return pathUtil.getSpaceId(remotePath.toString());
    }

    private String getContentId() {
        return pathUtil.getContentId(remotePath.toString());
    }

    private void log(String msg) {
        System.out.println(msg);
    }

}
