/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;

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
 * @author: Andrew Woods
 * Date: Aug 4, 2010
 */
public class ServiceResultProcessor implements ServiceResultListener {

    private final Logger log = LoggerFactory.getLogger(ServiceResultProcessor.class);

    public static final String STATUS_KEY = "processing-status";
    private static final String newline = System.getProperty("line.separator");

    private ContentStore contentStore;
    private String outputSpaceId;
    private String outputContentId;

    private long successfulResults = 0;
    private long unsuccessfulResults = 0;
    private long totalWorkitems = -1;

    private File resultsFile;


    public ServiceResultProcessor(ContentStore contentStore,
                                  String outputSpaceId,
                                  String outputContentId,
                                  String header,
                                  File workDir) {
        this.contentStore = contentStore;
        this.outputSpaceId = outputSpaceId;
        this.outputContentId = outputContentId;

        this.resultsFile = new File(workDir, outputContentId);
        writeToLocalResultsFile(header, false);
    }

    private void writeToLocalResultsFile(String text, boolean append) {
        try {
            FileWriter writer = new FileWriter(resultsFile, append);
            writer.append(text);
            writer.append(newline);
            writer.close();

        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Error writing result: '");
            sb.append(text);
            sb.append("' to results file: ");
            sb.append(resultsFile.getAbsolutePath());
            sb.append(", exception: ");
            sb.append(e.getMessage());
            log.error(sb.toString());
        }
    }

    public synchronized void processServiceResult(ServiceResult result) {
        if (result.isSuccess()) {
            successfulResults++;
        } else {
            unsuccessfulResults++;
        }

        writeToLocalResultsFile(result.getEntry(), true);

        InputStream resultsStream = getLocalResultsFileStream();
        try {
            contentStore.addContent(outputSpaceId,
                                    outputContentId,
                                    resultsStream,
                                    resultsFile.length(),
                                    "text/csv",
                                    null,
                                    null);
        } catch (ContentStoreException e) {
            log.error(
                "Error attempting to store service results: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(resultsStream);
        }
    }

    @Override
    public void setTotalWorkitems(long total) {
        this.totalWorkitems = total;
    }

    private InputStream getLocalResultsFileStream() {
        try {
            return new FileInputStream(resultsFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(
                "Could not create results stream: " + e.getMessage(), e);
        }
    }

    public String getProcessingStatus() {
        String status = "in-progress";
        if (isComplete()) {
            status = "complete";
        }

        StringBuilder sb = new StringBuilder(status);
        sb.append(": ");
        sb.append(successfulResults + unsuccessfulResults);
        sb.append("/");
        sb.append(totalIsAvailable() ? totalWorkitems : "?");

        if (unsuccessfulResults > 0) {
            String failSuffix = "failures";
            if (unsuccessfulResults == 1) {
                failSuffix = "failure";
            }
            sb.append(" [" + unsuccessfulResults + " " + failSuffix + "]");
        }
        return sb.toString();
    }

    private boolean isComplete() {
        return successfulResults + unsuccessfulResults == totalWorkitems;
    }

    @Override
    public void setProcessingComplete() {
        totalWorkitems = successfulResults + unsuccessfulResults;
    }

    private boolean totalIsAvailable() {
        return totalWorkitems != -1;
    }

}
