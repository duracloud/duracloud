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

    private String phase;
    private String previousPhaseStatus;

    private long successfulResults = 0;
    private long unsuccessfulResults = 0;
    private long totalWorkItems = -1;

    private State state;

    private File resultsFile;

    public ServiceResultProcessor(ContentStore contentStore,
                                  String outputSpaceId,
                                  String outputContentId,
                                  String phase,
                                  File workDir) {
        this(contentStore,
             outputSpaceId,
             outputContentId,
             phase,
             null,
             workDir);
    }

    public ServiceResultProcessor(ContentStore contentStore,
                                  String outputSpaceId,
                                  String outputContentId,
                                  String phase,
                                  String previousPhaseStatus,
                                  File workDir) {
        this.contentStore = contentStore;
        this.outputSpaceId = outputSpaceId;
        this.outputContentId = outputContentId;
        this.phase = phase;
        this.previousPhaseStatus = previousPhaseStatus;

        this.state = State.IN_PROGRESS;
        this.resultsFile = new File(workDir, outputContentId);
        if (resultsFile.exists()) {
            resultsFile.delete();
        }
    }

    public synchronized void processServiceResult(ServiceResult result) {
        writeToLocalResultsFile(result);

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

        if (result.isSuccess()) {
            successfulResults++;
        } else {
            unsuccessfulResults++;
        }
    }

    private void writeToLocalResultsFile(ServiceResult result) {
        if (!resultsFile.exists()) {
            write(result.getHeader());
        }
        write(result.getEntry());
    }

    private void write(String text) {
        boolean append = true;
        FileWriter writer = null;
        try {
            writer = new FileWriter(resultsFile, append);
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

    @Override
    public void setTotalWorkItems(long total) {
        this.totalWorkItems = total;
    }

    @Override
    public void setProcessingState(State state) {
        this.state = state;
    }

    private InputStream getLocalResultsFileStream() {
        try {
            return new FileInputStream(resultsFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(
                "Could not create results stream: " + e.getMessage(), e);
        }
    }

    public synchronized String getProcessingStatus() {
        return new StatusMsg(successfulResults,
                             unsuccessfulResults,
                             totalWorkItems,
                             state,
                             phase,
                             previousPhaseStatus).toString();
    }

}
