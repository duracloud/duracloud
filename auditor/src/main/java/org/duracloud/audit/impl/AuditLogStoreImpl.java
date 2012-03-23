/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.audit.AuditLogStore;
import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.DateUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.aop.ContentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: 3/19/12
 */
public class AuditLogStoreImpl implements AuditLogStore {

    private final Logger log = LoggerFactory.getLogger(AuditLogStoreImpl.class);

    private static final String newline = System.getProperty("line.separator");

    private ContentStore contentStore;
    private String auditLogSpaceId;
    private String auditLogPrefix;
    private long auditLogSizeLimit;

    public AuditLogStoreImpl(String auditLogSpaceId,
                             String auditLogPrefix,
                             long auditLogSizeLimit) {
        this.auditLogSpaceId = auditLogSpaceId;
        this.auditLogPrefix = auditLogPrefix;
        this.auditLogSizeLimit = auditLogSizeLimit;
    }

    @Override
    public void initialize(ContentStore contentStore) {
        this.contentStore = contentStore;
    }

    @Override
    public Iterator<String> logs(String spaceId) {
        checkInitialized();

        List<String> logs = new ArrayList<String>();
        Iterator<String> logsItr;
        try {
            logsItr = contentStore.getSpaceContents(auditLogSpaceId,
                                                    auditLogPrefix + spaceId);

        } catch (ContentStoreException e) {
            return logs.iterator();
        }

        while (logsItr.hasNext()) {
            String contentId = logsItr.next();
            if (matches(contentId, spaceId)) {
                logs.add(contentId);
            }
        }
        return logs.iterator();
    }

    @Override
    public void removeLog(String logContentId) {
        removeLogFromLocal(logContentId);
        removeLogFromStore(logContentId);
    }

    private void removeLogFromLocal(String logContentId) {
        File logFile = new File(getTmpDir(), logContentId);
        if (!logFile.exists()) {
            log.info("Log file !exist: {}", logFile.getAbsolutePath());

        } else {
            log.info("Deleting log file: {}", logFile.getAbsolutePath());
            FileUtils.deleteQuietly(logFile);
        }
    }

    private void removeLogFromStore(String logContentId) {
        boolean done = false;
        int tries = 0;
        final int maxTries = 3;
        while (!done && tries++ < maxTries) {
            try {
                doRemoveLog(logContentId);
                done = true;
            } catch (ContentStoreException e) {
                // do nothing
            }
        }

        if (!done) {
            StringBuilder err = new StringBuilder("Unable to delete log, ");
            err.append(auditLogSpaceId);
            err.append("/");
            err.append(logContentId);
            err.append(", after ");
            err.append(tries);
            err.append(" tries");
            log.error(err.toString());
            throw new DuraCloudRuntimeException(err.toString());
        }
    }

    private void doRemoveLog(String logContentId) throws ContentStoreException {
        try {
            contentStore.deleteContent(auditLogSpaceId, logContentId);

        } catch (ContentStoreException e) {
            log.warn("Error deleting log file: {}!", logContentId, e);
            throw e;
        }
    }

    @Override
    public void write(List<ContentMessage> events) {
        checkInitialized();

        if (null == events || events.size() == 0) {
            log.info("arg events are empty, nothing to write!");
        }

        String spaceId = events.get(0).getSpaceId();

        // Verify all events relate to a single space.
        for (ContentMessage event : events) {
            if (!spaceId.equals(event.getSpaceId())) {
                StringBuilder err = new StringBuilder("All arg events ");
                err.append("must be of the same spaceId: ");
                err.append(spaceId);
                err.append(" != ");
                err.append(event.getSpaceId());
                log.error(err.toString());
                throw new IllegalArgumentException(err.toString());
            }
        }

        // Get locally cached log file, or create a new file.
        File logFile = getLogFile(spaceId);
        boolean isNewFile = !logFile.exists();

        // Write header, if necessary.
        FileWriter fileWriter = getFileWriter(logFile);
        if (isNewFile) {
            write(fileWriter, ContentMessage.tsvHeader());
        }

        // Write events to log.
        for (ContentMessage event : events) {
            write(fileWriter, event.asTSV());
        }

        IOUtils.closeQuietly(fileWriter);

        // Push log to content store.
        addContent(logFile);
    }

    private FileWriter getFileWriter(File logFile) {
        boolean append = true;
        try {
            return new FileWriter(logFile, append);
        } catch (IOException e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    private void addContent(File logFile) {
        boolean done = false;
        int tries = 0;
        final int maxTries = 3;
        while (!done && tries++ < maxTries) {
            try {
                doAddContent(logFile);
                done = true;
            } catch (ContentStoreException e) {
                // do nothing
            }
        }

        if (!done) {
            StringBuilder err = new StringBuilder("Unable to add audit log, ");
            err.append(logFile.getAbsolutePath());
            err.append(", after ");
            err.append(tries);
            err.append(" tries");
            log.error(err.toString());
            throw new DuraCloudRuntimeException(err.toString());
        }
    }

    private void doAddContent(File logFile) throws ContentStoreException {
        String auditLogContentId = getContentId(logFile);
        InputStream auditLogInputStream = getInputStream(logFile);
        long auditLogSize = FileUtils.sizeOf(logFile);
        String auditLogMimeType = "text/tab-separated-values";
        String auditLogChecksum = getChecksum(logFile);

        try {
            contentStore.addContent(auditLogSpaceId,
                                    auditLogContentId,
                                    auditLogInputStream,
                                    auditLogSize,
                                    auditLogMimeType,
                                    auditLogChecksum,
                                    null);
        } catch (ContentStoreException e) {
            log.warn("Error adding log file!", e);
            throw e;
        } finally {
            IOUtils.closeQuietly(auditLogInputStream);
        }
    }

    private void write(FileWriter fileWriter, String text) {
        try {
            fileWriter.append(text);
            fileWriter.append(newline);

        } catch (IOException e) {
            log.error("Error writing audit log!", e);
        }
    }

    private String getContentId(File file) {
        String tmpDirPath = getTmpDir().getPath();
        String logPath = file.getPath();
        String contentId = logPath.substring(tmpDirPath.length());

        while (contentId.startsWith(File.separator)) {
            contentId = contentId.substring(1);
        }

        return contentId;
    }

    private InputStream getInputStream(File logFile) {
        try {
            return FileUtils.openInputStream(logFile);

        } catch (IOException e) {
            log.error("Error getting input stream for log file: {}",
                      logFile.getAbsolutePath(),
                      e);
        }
        return null;
    }

    private String getChecksum(File logFile) {
        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        try {
            return util.generateChecksum(logFile);

        } catch (IOException e) {
            log.error("Error generating checksum for logfile: {}",
                      logFile.getAbsolutePath(),
                      e);
        }
        return null;
    }

    /**
     * This method is 'protected' for testing access.
     *
     * @param spaceId of with audit events
     * @return local audit log file
     */
    protected File getLogFile(String spaceId) {
        File logFile = null;
        String logContentId = null;

        File tmpDir = getTmpDir();
        String[] exts = new String[]{"tsv"};
        Iterator<File> fileItr = FileUtils.iterateFiles(tmpDir, exts, true);

        // Search for locally cached log file.
        while (fileItr.hasNext()) {
            File file = fileItr.next();
            String contentId = getContentId(file);
            if (matches(contentId, spaceId)) {

                if (null == logFile) {
                    logFile = file;
                    logContentId = contentId;

                } else if (logContentId.compareTo(contentId) < 0) {
                    logFile = file;
                    logContentId = contentId;
                }
            }
        }

        // Search for existing log file in store.
        if (null == logFile) {
            logFile = getLogFileFromStore(spaceId);
        }

        // Apparently no log file exists, create one.
        if (null == logFile || logFile.length() > auditLogSizeLimit) {
            logFile = newLogFile(spaceId);
        }

        return logFile;
    }

    private File getLogFileFromStore(String spaceId) {
        List<String> logContentIds = new ArrayList<String>();

        Iterator<String> contents = logs(spaceId);
        while (contents.hasNext()) {
            logContentIds.add(contents.next());
        }

        if (logContentIds.size() == 0) {
            return null;
        }

        // Select the most current log.
        String currentLog = logContentIds.get(0);
        for (String contentId : logContentIds) {
            if (currentLog.compareTo(contentId) < 0) {
                currentLog = contentId;
            }
        }

        Content content = getContent(currentLog);

        File logFile = new File(getTmpDir(), currentLog);
        mkdir(logFile);

        OutputStream logFileOutputStream = getOutputStream(logFile);

        InputStream contentInputStream = content.getStream();
        copy(logFileOutputStream, contentInputStream);

        return logFile;
    }

    private boolean matches(String contentId, String spaceId) {
        if (null == contentId) {
            return false;
        }

        String prefix = auditLogPrefix + spaceId;
        return contentId.matches(
            prefix + "-\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.tsv");
    }

    private void copy(OutputStream logFileOutputStream,
                      InputStream contentInputStream) {
        try {
            IOUtils.copy(contentInputStream, logFileOutputStream);

        } catch (IOException e) {
            StringBuilder err = new StringBuilder("Error copying streams.");
            log.error(err.toString(), e);
            throw new DuraCloudRuntimeException(err.toString(), e);
        } finally {
            IOUtils.closeQuietly(contentInputStream);
            IOUtils.closeQuietly(logFileOutputStream);
        }
    }

    private OutputStream getOutputStream(File logFile) {
        try {
            return FileUtils.openOutputStream(logFile);

        } catch (IOException e) {
            StringBuilder err = new StringBuilder("Error outputStream: ");
            err.append(logFile.getAbsolutePath());
            log.error(err.toString(), e);
            throw new DuraCloudRuntimeException(err.toString(), e);
        }
    }

    private Content getContent(String currentLog) {
        Content content = null;

        boolean done = false;
        int tries = 0;
        final int maxTries = 3;
        while (!done && tries++ < maxTries) {
            try {
                content = doGetContent(currentLog);
                done = true;
            } catch (ContentStoreException e) {
                // do nothing
            }
        }

        if (!done) {
            StringBuilder err = new StringBuilder("Unable to get log, ");
            err.append(currentLog);
            err.append(", after ");
            err.append(tries);
            err.append(" tries");
            log.error(err.toString());
            throw new DuraCloudRuntimeException(err.toString());
        }

        return content;
    }

    private Content doGetContent(String currentLog)
        throws ContentStoreException {
        Content content;
        try {
            content = contentStore.getContent(auditLogSpaceId, currentLog);
        } catch (ContentStoreException e) {
            log.warn("Error getting: {}/{}", auditLogSpaceId, currentLog);
            throw e;
        }
        return content;
    }

    private File newLogFile(String spaceId) {
        String prefix = auditLogPrefix + spaceId;
        String fullName = prefix + "-" + DateUtil.now() + ".tsv";

        File logFile = new File(getTmpDir(), fullName);
        mkdir(logFile);

        return logFile;
    }

    private void mkdir(File file) {
        try {
            FileUtils.forceMkdir(file.getParentFile());
        } catch (IOException e) {
            // do nothing
        }
    }

    private File getTmpDir() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        return new File(tmpDir);
    }

    private void checkInitialized() {
        if (null == contentStore) {
            StringBuilder err = new StringBuilder("AuditLogStore must be ");
            err.append("initialized!");
            log.error(err.toString());
            throw new DuraCloudRuntimeException(err.toString());
        }
    }

}
