/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.audit.Auditor;
import org.duracloud.audit.error.AuditLogNotFoundException;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.util.DateUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.manifest.ManifestFormatter;
import org.duracloud.manifest.ManifestGenerator;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestEmptyException;
import org.duracloud.manifest.error.ManifestGeneratorException;
import org.duracloud.storage.aop.ContentMessage;
import org.duracloud.storage.error.InvalidEventTSVException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class manages the generation of content manifests.
 *
 * @author Andrew Woods
 *         Date: 3/27/12
 */
public class ManifestGeneratorImpl implements ManifestGenerator {

    private final Logger log =
        LoggerFactory.getLogger(ManifestGeneratorImpl.class);

    private FileCleaningTracker fileCleaningTracker;
    private ContentStoreManager storeManager;
    private Auditor auditor;
    private String auditLogSpace;

    private Map<String, ContentMessage> events; // contentId -> event

    public ManifestGeneratorImpl(Auditor auditor,
                                 String auditLogSpace,
                                 FileCleaningTracker fileCleaningTracker) {
        this.auditor = auditor;
        this.auditLogSpace = auditLogSpace;
        this.fileCleaningTracker = fileCleaningTracker;
        this.events = new HashMap<String, ContentMessage>();
    }

    @Override
    public void initialize(ContentStoreManager storeManager) {
        this.storeManager = storeManager;
        this.events = new HashMap<String, ContentMessage>();
    }

    @Override
    public InputStream getManifest(String storeId,
                                   String spaceId,
                                   FORMAT format,
                                   Date asOfDate)
        throws ManifestArgumentException, ManifestEmptyException {
        List<String> logs = getAuditLogs(storeId, spaceId);
        for (String log : logs) {
            scanLog(log, storeId, asOfDate);
        }

        // Create the manifest and get a handle to its input stream.
        File manifest = buildManifest(format);
        InputStream stream = stream(manifest);

        // Register the temp file to be removed when the stream is out of scope.
        cleanup(manifest, stream);

        return stream;
    }

    private void cleanup(File file, Object marker) {
        // remove temp manifest either on jvm exit or
        //  when marker goes out of scope.
        file.deleteOnExit();
        fileCleaningTracker.track(file, marker);

        // clear existing events.
        events.clear();
    }

    private void scanLog(String logContentId, String storeId, Date asOfDate) {
        Iterator<String> lines = getLogIterator(logContentId);
        while (lines.hasNext()) {
            ContentMessage event = getEvent(lines.next());

            log.debug("scanning event: {}", event);
            if (null != event &&
                matchesStoreId(event, storeId) &&
                preceedsDate(event, asOfDate)) {

                switch (ContentMessage.ACTION.valueOf(event.getAction())) {
                    case INGEST:
                    case COPY:
                        events.put(event.getContentId(), event);
                        break;
                    case DELETE:
                        events.remove(event.getContentId());
                        break;
                    case ERROR:
                        log.warn("Corrupted event: " + event);
                        break;
                    default:
                        log.error("Unexpected event: " + event);
                        break;
                }
            }
        }
    }

    private ContentMessage getEvent(String line) {
        ContentMessage event = null;

        if (line == null || line.isEmpty() ||
            line.equals(ContentMessage.tsvHeader())) {
            return event;
        }

        try {
            event = new ContentMessage(line);

        } catch (InvalidEventTSVException e) {
            log.error(e.getMessage());
        }
        return event;
    }

    private boolean matchesStoreId(ContentMessage event, String storeId) {
        if (null == storeId) {
            return true;
        }

        return storeId.equals(event.getStoreId());
    }

    private boolean preceedsDate(ContentMessage event, Date asOfDate) {
        if (null == asOfDate) {
            return true;
        }

        Date date = getDate(event.getDatetime());
        if (null == date) {
            return false;
        }

        return !date.after(asOfDate);
    }

    private Date getDate(String text) {
        Exception exception = null;
        for (DateUtil.DateFormat dateFormat : DateUtil.DateFormat.values()) {
            try {
                return DateUtil.convertToDate(text, dateFormat);
            } catch (ParseException e) {
                exception = e;
            }
        }

        log.error(exception.getMessage());
        return null;
    }

    private File buildManifest(FORMAT format) throws ManifestArgumentException {

        ManifestFormatter formatter;
        switch (format) {
            case BAGIT:
                formatter = new BagitManifestFormatter();
                break;
            case TSV:
                formatter = new TsvManifestFormatter();
                break;
            default:
                String err = "Unexpected format: " + format.name();
                log.error(err);
                throw new ManifestArgumentException(err);
        }

        File file = getTempFile();
        OutputStream output = outputStream(file);

        formatter.writeEventsToOutput(events.values(), output);

        IOUtils.closeQuietly(output);
        return file;
    }

    private OutputStream outputStream(File file) {
        try {
            return FileUtils.openOutputStream(file);

        } catch (IOException e) {
            StringBuilder err = new StringBuilder("Error ");
            err.append("opening output stream: ");
            err.append(file.getAbsolutePath());
            err.append(", error: ");
            err.append(e.getMessage());
            log.error(err.toString());

            throw new ManifestGeneratorException(err.toString(), e);
        }
    }

    private File getTempFile() {
        try {
            return File.createTempFile(DateUtil.nowPlain() + "-", "-manifest-gen");

        } catch (IOException e) {
            StringBuilder err = new StringBuilder("Error creating temp file, ");
            err.append("error: ");
            err.append(e.getMessage());
            log.error(err.toString());

            throw new ManifestGeneratorException(err.toString(), e);
        }
    }

    private List<String> getAuditLogs(String storeId,
                                      String spaceId)
        throws ManifestEmptyException {
        try {
            return auditor.getAuditLogs(spaceId);

        } catch (AuditLogNotFoundException e) {
            StringBuilder err = new StringBuilder("Audit log not found, ");
            err.append(storeId);
            err.append(":");
            err.append(spaceId);
            err.append(", message: ");
            err.append(e.getMessage());
            log.warn(err.toString());
            throw new ManifestEmptyException(err.toString(), e);
        }
    }

    private Iterator<String> getLogIterator(String logContentId) {
        InputStream logStream = getLogStream(logContentId);
        Iterator<String> lines;
        try {
            lines = IOUtils.lineIterator(logStream, null);

        } catch (IOException e) {
            log.warn("Error getting line iterator for: {}", logContentId, e);
            lines = new ArrayList<String>().iterator();
        }
        return lines;
    }

    private InputStream getLogStream(String logContentId) {
        Content content;
        try {
            content = getContentStore().getContent(auditLogSpace, logContentId);

        } catch (ContentStoreException e) {
            StringBuilder err = new StringBuilder("Error getting log: ");
            err.append(auditLogSpace);
            err.append("//");
            err.append(logContentId);
            err.append(", error: ");
            err.append(e.getMessage());
            log.error(err.toString());

            throw new ManifestGeneratorException(err.toString(), e);
        }
        return content.getStream();
    }

    private ContentStore getContentStore() throws ContentStoreException {
        return storeManager.getPrimaryContentStore();
    }

    private InputStream stream(File file) {
        try {
            return FileUtils.openInputStream(file);

        } catch (IOException e) {
            StringBuilder err = new StringBuilder("Error opening file: ");
            err.append(file.getAbsolutePath());
            err.append(", error: ");
            err.append(e.getMessage());
            log.error(err.toString());

            throw new ManifestGeneratorException(err.toString(), e);
        }
    }

}
