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
    private File resultsFile;

    public DuplicationResultListener(ContentStore contentStore,
                                     String spaceId,
                                     String reportId,
                                     String workDir) {
        this.contentStore = contentStore;
        this.spaceId = spaceId;
        this.reportId = reportId;

        this.resultsFile = new File(workDir, this.reportId);
        if (resultsFile.exists()) {
            resultsFile.delete();
        }
    }

    @Override
    public void processResult(DuplicationEvent event) {
        log.debug("processing event: {}", event);

        if (isRecursiveUpdate(event)) {
            log.debug("not propagating recursive update: {}", event);
            return;
        }

        writeToLocalResultsFile(event);
        InputStream resultsStream = getLocalResultsFileStream();
        try {
            contentStore.addContent(spaceId,
                                    reportId,
                                    resultsStream,
                                    resultsFile.length(),
                                    "text/tab-separated-values",
                                    null,
                                    null);

        } catch (ContentStoreException e) {
            String err = "Error attempting to store duplication service " +
                "results to {}/{}, due to: {}";
            log.error(err, new Object[]{spaceId, reportId, e.getMessage()});

        } finally {
            IOUtils.closeQuietly(resultsStream);
        }
    }

    private boolean isRecursiveUpdate(DuplicationEvent event) {
        return spaceId.equals(event.getSpaceId()) &&
            reportId.equals(event.getContentId());
    }

    private void writeToLocalResultsFile(DuplicationEvent event) {
        if (!resultsFile.exists()) {
            mkdir(resultsFile);
            write(event.getHeader());
        }
        write(event.getEntry());
    }

    private void write(String text) {
        boolean append = true;
        FileWriter writer;
        try {
            writer = new FileWriter(resultsFile, append);
            writer.append(text);
            writer.append(newline);
            writer.close();

        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Error writing event: '");
            sb.append(text);
            sb.append("' to results file: ");
            sb.append(resultsFile.getAbsolutePath());
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

    private InputStream getLocalResultsFileStream() {
        try {
            return new FileInputStream(resultsFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(
                "Could not create results stream: " + e.getMessage(), e);
        }
    }

}
