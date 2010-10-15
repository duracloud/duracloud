/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.retrieval.source.ContentItem;
import org.duracloud.retrieval.source.ContentStream;
import org.duracloud.retrieval.source.RetrievalSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handles the retrieving of a single file from DuraCloud.
 *
 * @author: Bill Branan
 * Date: Oct 12, 2010
 */
public class RetrievalWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(RetrievalWorker.class);

    private static final int MAX_ATTEMPTS = 5;
    private static final String COPY = "-copy";

    private ContentItem contentItem;
    private RetrievalSource source;
    private File contentDir;
    private boolean overwrite;
    private OutputWriter outWriter;
    private int attempts;

    private StatusManager statusManager;

    /**
     * Creates a Retrieval Worker to handle retrieving a file
     */
    public RetrievalWorker(ContentItem contentItem,
                           RetrievalSource source,
                           File contentDir,
                           boolean overwrite,
                           OutputWriter outWriter) {
        this.contentItem = contentItem;
        this.source = source;
        this.contentDir = contentDir;
        this.overwrite = overwrite;
        this.outWriter = outWriter;
        this.statusManager = StatusManager.getInstance();
        this.attempts = 0;
    }

    public void run() {
        statusManager.startingWork();
        retrieveFile();
    }

    protected void retrieveFile() {
        attempts++;
        File localFile = getLocalFile();
        try {
            if(localFile.exists()) { // File already exists
                if(checksumsMatch(localFile)) {
                    noChangeNeeded(localFile.getAbsolutePath());
                } else { // Different file in DuraStore
                    if(overwrite) {
                        deleteFile(localFile);
                    } else {
                        renameFile(localFile);
                    }
                    retrieveToFile(localFile);
                    succeed(localFile.getAbsolutePath());
                }
            } else { // File does not exist
                File parentDir = localFile.getParentFile();
                if(!parentDir.exists()) {
                    parentDir.mkdirs();
                    parentDir.setWritable(true);
                }
                retrieveToFile(localFile);
                succeed(localFile.getAbsolutePath());
            }
        } catch(Exception e) {
            logger.error("Exception retrieving remote file " +
                         contentItem.getContentId() + " as local file " +
                         localFile.getAbsolutePath() + ": " + e.getMessage(), e);
            if(attempts < MAX_ATTEMPTS) {
                retrieveFile();
            } else {
                fail(e.getMessage());
            }
        }
    }

    /*
     * Gets the local storage file for the content item
     */
    protected File getLocalFile() {
        File spaceDir = new File(contentDir, contentItem.getSpaceId());
        return new File(spaceDir, contentItem.getContentId());
    }

    /*
     * Checks to see if the checksums of the local file and remote file match
     */
    protected boolean checksumsMatch(File localFile) throws IOException {
        ChecksumUtil checksumUtil =
            new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String localChecksum = checksumUtil.generateChecksum(localFile);
        String remoteChecksum = source.getSourceChecksum(contentItem);
        return localChecksum.equals(remoteChecksum);
    }

    /*
     * Renames the given file, returns the copied file. Does not change
     * the original passed in file path.
     */
    protected File renameFile(File localFile) throws IOException {
        File origFile = new File(localFile.getAbsolutePath());
        File copiedFile = new File(localFile.getParent(),
                                   localFile.getName() + COPY);
        for(int i=2; copiedFile.exists(); i++) {
            copiedFile = new File(localFile.getParent(),
                                  localFile.getName() + COPY + "-" + i);
        }
        FileUtils.moveFile(origFile, copiedFile);
        return copiedFile;
    }

    /*
     * Deletes a local file
     */
    protected void deleteFile(File localFile) throws IOException {
        localFile.delete();
    }

    /*
     * Transfers the remote file stream to the local file
     */
    protected void retrieveToFile(File localFile) throws IOException {
        ContentStream content = source.getSourceContent(contentItem);

        InputStream inStream = content.getStream();
        OutputStream outStream = new FileOutputStream(localFile);
        try {
            IOUtils.copyLarge(inStream, outStream);
        } finally {
            if(inStream != null) {
                inStream.close();
            }

            if(outStream != null) {
                outStream.close();
            }
        }
    }

    protected void noChangeNeeded(String localFilePath) {
        if(logger.isDebugEnabled()) {
            logger.debug("Local file " + localFilePath +
                         " matches remote file " + contentItem.toString() +
                         " no update needed");
        }
        statusManager.noChangeCompletion();
    }

    protected void succeed(String localFilePath) {
        if(logger.isDebugEnabled()) {
            logger.debug("Successfully retrieved " + contentItem.toString() +
                         " to local file " + localFilePath);
        }
        outWriter.writeSuccess(contentItem, localFilePath, attempts);
        statusManager.successfulCompletion();
    }

    protected void fail(String errMsg) {
        String error = "Failed to retrieve " + contentItem.toString() +
                       " after " + attempts +
                       " attempts. Last error message was: " + errMsg;
        logger.error(error);
        System.err.println(error);
        outWriter.writeFailure(contentItem, error, attempts);
        statusManager.failedCompletion();
    }

}
