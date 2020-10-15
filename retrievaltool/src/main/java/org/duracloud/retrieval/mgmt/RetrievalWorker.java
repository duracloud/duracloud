/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.chunk.util.ChunkUtil;
import org.duracloud.client.ContentStore;
import org.duracloud.common.model.ContentItem;
import org.duracloud.common.retry.Retrier;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.DateUtil;
import org.duracloud.retrieval.source.ContentStream;
import org.duracloud.retrieval.source.RetrievalSource;
import org.duracloud.stitch.error.MissingContentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private boolean createSpaceDir;
    private boolean applyTimestamps;
    private int attempts;
    private File localFile;
    private ContentStream contentStream;

    private StatusManager statusManager;

    /**
     * Creates a Retrieval Worker to handle retrieving a file
     */
    public RetrievalWorker(ContentItem contentItem,
                           RetrievalSource source,
                           File contentDir,
                           boolean overwrite,
                           OutputWriter outWriter,
                           boolean createSpaceDir,
                           boolean applyTimestamps) {
        this.contentItem = contentItem;
        this.source = source;
        this.contentDir = contentDir;
        this.overwrite = overwrite;
        this.outWriter = outWriter;
        this.createSpaceDir = createSpaceDir;
        this.applyTimestamps = applyTimestamps;
        this.statusManager = StatusManager.getInstance();
        this.attempts = 0;
    }

    public void run() {
        try {
            statusManager.startingWork();
            retrieveFile();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    public Map<String, String> retrieveFile() {
        return retrieveFile(null);
    }

    public Map<String, String> retrieveFile(RetrievalListener listener) {
        attempts++;
        File localFile = getLocalFile();
        Map<String, String> props = null;
        try {
            if (localFile.exists()) { // File already exists
                props = getContentProperties();
                if (checksumsMatch(localFile,
                                   props.get(ContentStore.CONTENT_CHECKSUM))) {
                    noChangeNeeded(localFile.getAbsolutePath());
                } else { // Different file in DuraStore
                    if (overwrite) {
                        deleteFile(localFile);
                    } else {
                        renameFile(localFile);
                    }
                    props = retrieveToFile(localFile, listener);
                    succeed(localFile.getAbsolutePath());
                }
            } else { // File does not exist
                File parentDir = localFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                    parentDir.setWritable(true);
                }
                props = retrieveToFile(localFile, listener);
                succeed(localFile.getAbsolutePath());
            }
        } catch (MissingContentException mce) {
            missing(mce.getMessage());
        } catch (Throwable e) {
            logger.error("Exception retrieving remote file " +
                         contentItem.getContentId() + " as local file " +
                         localFile.getAbsolutePath() + ": " + e.getMessage(), e);
            if (attempts < MAX_ATTEMPTS) {
                props = retrieveFile();
            } else {
                fail(e.getMessage());
            }
        }
        return props;
    }

    /*
     * Gets the local storage file for the content item
     */
    public File getLocalFile() {
        if (this.localFile == null) {
            ChunkUtil util = new ChunkUtil();
            String contentId = contentItem.getContentId();
            if (util.isChunkManifest(contentId)) {
                contentId = util.preChunkedContentId(contentId);
            }

            if (createSpaceDir) {
                File spaceDir = new File(contentDir, contentItem.getSpaceId());
                logger.debug("spaceDir.absolutePath={}", spaceDir.getAbsolutePath());
                this.localFile = new File(spaceDir, contentId);
            } else {
                this.localFile = new File(contentDir, contentId);
            }
        }

        logger.debug("localFile.absolutePath={}", this.localFile.getAbsolutePath());

        return this.localFile;
    }

    protected boolean checksumsMatch(File localFile) throws IOException {
        return checksumsMatch(localFile, null);
    }

    /*
     * Checks to see if the checksums of the local file and remote file match
     */
    protected boolean checksumsMatch(File localFile, String remoteChecksum)
        throws IOException {
        if (remoteChecksum == null || "".equals(remoteChecksum)) {
            if (contentStream != null) {
                remoteChecksum = contentStream.getChecksum();
            } else {
                remoteChecksum = source.getSourceChecksum(contentItem);
            }
        }
        String localChecksum = getChecksum(localFile);
        return localChecksum.equals(remoteChecksum);
    }

    protected String getChecksum(File localFile) throws IOException {
        ChecksumUtil checksumUtil =
            new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String localChecksum = checksumUtil.generateChecksum(localFile);
        return localChecksum;
    }

    /*
     * Renames the given file, returns the copied file. Does not change
     * the original passed in file path.
     */
    protected File renameFile(File localFile) throws IOException {
        File origFile = new File(localFile.getAbsolutePath());
        File copiedFile = new File(localFile.getParent(),
                                   localFile.getName() + COPY);
        for (int i = 2; copiedFile.exists(); i++) {
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

    protected Map<String, String> getContentProperties() {
        Map<String, String> properties = null;
        if (contentStream != null) {
            properties = contentStream.getProperties();
        } else {
            properties = source.getSourceProperties(contentItem);
        }
        return properties;
    }

    /**
     * Transfers the remote file stream to the local file
     *
     * @param localFile
     * @param listener
     * @return
     * @throws IOException
     * @returns the checksum of the File upon successful retrieval.  Successful
     * retrieval means the checksum of the local file and remote file match,
     * otherwise an IOException is thrown.
     */
    protected Map<String, String> retrieveToFile(File localFile, RetrievalListener listener) throws IOException {

        try {
            contentStream = new Retrier(5, 4000, 3).execute(() -> {
                return source.getSourceContent(contentItem, listener);
            });
        } catch (MissingContentException mce) {
            throw mce;
        } catch (Exception ex) {
            throw new IOException(ex);
        }

        try (
            InputStream inStream = contentStream.getStream();
            OutputStream outStream = new FileOutputStream(localFile);
        ) {
            IOUtils.copyLarge(inStream, outStream);
        } catch (IOException e) {
            try {
                deleteFile(localFile);
            } catch (IOException ioe) {
                logger.error("Exception deleting local file " +
                             localFile.getAbsolutePath() + " due to: " + ioe.getMessage());
            }
            throw e;
        }

        if (!checksumsMatch(localFile, contentStream.getChecksum())) {
            deleteFile(localFile);
            throw new IOException("Calculated checksum value for retrieved " +
                                  "file does not match properties checksum.");
        }

        // Set time stamps
        if (applyTimestamps) {
            applyTimestamps(contentStream, localFile);
        }
        return contentStream.getProperties();
    }

    /*
     * Applies timestamps which are found in the content item's properties
     * to the retrieved file
     */
    protected void applyTimestamps(ContentStream content, File localFile) {
        FileTime createTime =
            convertDateToFileTime(content.getDateCreated());
        FileTime lastAccessTime =
            convertDateToFileTime(content.getDateLastAccessed());
        FileTime lastModTime =
            convertDateToFileTime(content.getDateLastModified());

        BasicFileAttributeView fileAttributeView =
            Files.getFileAttributeView(localFile.toPath(),
                                       BasicFileAttributeView.class,
                                       LinkOption.NOFOLLOW_LINKS);
        // If any time value is null, that value is left unchanged
        try {
            fileAttributeView.setTimes(lastModTime, lastAccessTime, createTime);
        } catch (IOException e) {
            logger.error("Error setting timestamps for local file " +
                         localFile.getAbsolutePath() + ": " + e.getMessage(),
                         e);
        }
    }

    /*
     * Converts a date in LONG string format to a FileTime object
     */
    private FileTime convertDateToFileTime(String strDate) {
        FileTime time = null;
        if (null != strDate) {
            Date date = null;
            try {
                date = DateUtil.convertToDate(strDate,
                                              DateUtil.DateFormat.LONG_FORMAT);
            } catch (ParseException e) {
                date = null;
            }

            if (null != date) {
                time = FileTime.fromMillis(date.getTime());
            }
        }
        return time;
    }

    protected void noChangeNeeded(String localFilePath) {
        if (logger.isDebugEnabled()) {
            logger.debug("Local file " + localFilePath +
                         " matches remote file " + contentItem.toString() +
                         " no update needed");
        }
        statusManager.noChangeCompletion();
    }

    protected void succeed(String localFilePath) {
        if (logger.isDebugEnabled()) {
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
        outWriter.writeFailure(contentItem, error, attempts);
        statusManager.failedCompletion();
    }

    protected void missing(String msg) {
        String message = "Unable to retrieve " + contentItem.toString() +
                         " because it doesn't exist in the space.";
        logger.error(message);
        outWriter.writeMissing(contentItem, message, attempts);
        statusManager.missingCompletion();
    }
}
