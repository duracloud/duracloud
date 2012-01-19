/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.result;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class listens for duplication events and reports on their success or
 * failure.
 *
 * @author Andrew Woods
 *         Date: 9/14/11
 */
public class DuplicationResultListener implements ResultListener {

    private final Logger log =
        LoggerFactory.getLogger(DuplicationResultListener.class);

    private static final String newline = System.getProperty("line.separator");

    private ContentStore contentStore;
    private String spaceId;
    private String reportId;
    private String errorReportId;
    private File resultsFile;
    private File errorsFile;
    private long passCount = 0;
    private long failedCount = 0;

    public DuplicationResultListener(ContentStore contentStore,
                                     String spaceId,
                                     String reportId,
                                     String errorReportId,
                                     String workDir) {
        this.contentStore = contentStore;
        this.spaceId = spaceId;
        this.reportId = reportId;
        this.errorReportId = errorReportId;
        this.resultsFile = createFile(workDir, this.reportId);
        this.errorsFile  = createFile(workDir, this.errorReportId);
    }

    private File createFile(String workDir, String filename) {
        File file = new File(workDir, filename);
        if (file.exists()) {
            file.delete();
        }
        return file;
     }

    @Override
    public void processResult(DuplicationEvent event) {
        log.debug("processing event: {}", event);

        if (isRecursiveUpdate(event)) {
            log.debug("not propagating recursive update: {}", event);
            return;
        }

        writeResults(event);
    }

    private boolean isRecursiveUpdate(DuplicationEvent event) {
        return spaceId.equals(event.getSpaceId()) &&
            reportId.equals(event.getContentId());
    }

    private void writeResults(DuplicationEvent event) {
        writeResult(event, resultsFile, spaceId, reportId);
        if(!event.isSuccess()){
            writeResult(event, errorsFile, spaceId, errorReportId);
            failedCount++;
        }else{
            passCount++;
        }
    }
    
    private void writeResult(DuplicationEvent event,
                             File file,
                             String spaceId,
                             String contentId) {
        writeToFile(event, file);
        persistToCloud(file, spaceId, contentId);
    }

    private void persistToCloud(File file, String spaceId, String contentId){
        InputStream stream = getLocalResultsFileStream(file);
        try {
            contentStore.addContent(spaceId,
                                    contentId,
                                    stream,
                                    file.length(),
                                    "text/tab-separated-values",
                                    null,
                                    null);

        } catch (ContentStoreException e) {
            String err = "Error attempting to store duplication service " +
                "results to {}/{}, due to: {}";
            log.error(err, new Object[]{spaceId, contentId, e.getMessage()});

        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private void writeToFile(DuplicationEvent event, File file) {
        if (!file.exists()) {
            mkdir(file);
            write(file, event.getHeader());
        }
        write(file, event.getEntry());
    }

    private void write(File file, String text) {
        boolean append = true;
        FileWriter writer;
        try {
            writer = new FileWriter(file, append);
            writer.append(text);
            writer.append(newline);
            writer.close();

        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Error writing event: '");
            sb.append(text);
            sb.append("' to file: ");
            sb.append(file.getAbsolutePath());
            sb.append(", exception: ");
            sb.append(e.getMessage());
            log.error(sb.toString());
        }
    }

    private void mkdir(File file) {
        try {
            FileUtils.forceMkdir(file.getParentFile());
        } catch (IOException e) {
            // do nothing
        }
    }

    private InputStream getLocalResultsFileStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not create file stream: "
                + file.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    public long getPassCount() {
        return this.passCount;
    }

    public long getFailedCount() {
        return this.failedCount;
    }

}
