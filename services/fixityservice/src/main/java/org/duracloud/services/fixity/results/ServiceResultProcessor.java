/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.status.StatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Andrew Woods
 * Date: Aug 4, 2010
 */
public class ServiceResultProcessor implements ServiceResultListener {

    private final Logger log = LoggerFactory.getLogger(ServiceResultProcessor.class);

    public static final String STATUS_KEY = "processing-status";
    private static final String newline = System.getProperty("line.separator");
    private ContentStore contentStore;
    private StatusListener statusListener;
    private String outputSpaceId;
    private String outputContentId;
    private String errorContentId;
    
    private String phase;
    private String previousPhaseStatus;

    private long successfulResults = 0;
    private long unsuccessfulResults = 0;
    private long totalWorkItems = -1;

    private State state;

    private File resultsFile;
    private File errorsFile;
    

    public ServiceResultProcessor(ContentStore contentStore,
                                  StatusListener statusListener,
                                  String outputSpaceId,
                                  String outputContentId,
                                  String errorContentId,
                                  String phase,
                                  File workDir) {
        this(contentStore,
             statusListener,
             outputSpaceId,
             outputContentId,
             errorContentId,
             phase,
             null,
             workDir);
    }

    public ServiceResultProcessor(ContentStore contentStore,
                                  StatusListener statusListener,
                                  String outputSpaceId,
                                  String outputContentId,
                                  String errorContentId,
                                  String phase,
                                  String previousPhaseStatus,
                                  File workDir) {
        this.contentStore = contentStore;
        this.statusListener = statusListener;
        this.outputSpaceId = outputSpaceId;
        this.outputContentId = outputContentId;
        this.errorContentId = errorContentId;
        this.phase = phase;
        this.previousPhaseStatus = previousPhaseStatus;

        this.state = State.IN_PROGRESS;

        this.resultsFile = createFile(workDir, outputContentId);
        if(errorContentId != null){
            this.errorsFile = createFile(workDir, errorContentId);
        }
    }

    private File createFile(File workDir, String contentId) {
        File file = new File(workDir, contentId);
        if (file.exists()) {
            file.delete();
        }
        
        return file;
    }

    public synchronized void processServiceResult(ServiceResult result) {
        writeToLocalFile(resultsFile, result);
        storeFile(resultsFile, outputSpaceId, outputContentId);

        Collection<ServiceResultItem> items = result.getItems();
        if (items != null && items.size() > 0) {
            for (ServiceResultItem sr : items) {
                countSuccessesAndLogFailures(sr.isSuccess(),
                                             result.getHeader(),
                                             sr.getEntry());
            }
        } else {
            countSuccessesAndLogFailures(result.isSuccess(),
                                         result.getHeader(),
                                         result.getEntry());
        }

        if (errorsFile != null && !result.isSuccess()) {
            storeFile(errorsFile, outputSpaceId, errorContentId);
        }

    }

    private void countSuccessesAndLogFailures(boolean success, String header, String entry) {
        if (success) {
            successfulResults++;
        } else {
            unsuccessfulResults++;
            if(errorsFile != null){
                writeToLocalFile(errorsFile, header, entry);
            }
        }
    }

    private void storeFile(File file, String spaceId, String contentId) {
        InputStream stream = getLocalFileStream(file);
        try {
            contentStore.addContent(spaceId,
                                    contentId,
                                    stream,
                                    file.length(),
                                    "text/tab-separated-values",
                                    null,
                                    null);
        } catch (ContentStoreException e) {
            log.error(
                "Error attempting to store service result file: [" + 
                    file.getAbsolutePath()+"] " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private void writeToLocalFile(File file, ServiceResult result) {
        writeToLocalFile(file, result.getHeader(), result.getEntry());
    }
    
    private void writeToLocalFile(File file, String header, String entry) {
        if (!file.exists()) {
            mkdir(file);
            write(file, header);
        }
        write(file, entry);
    }

    private void write(File file, String text) {
        boolean append = true;
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, append);
            writer.append(text);
            writer.append(newline);
            writer.close();

        } catch (IOException e) {
            StringBuilder sb = new StringBuilder("Error writing result: '");
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

    @Override
    public void setTotalWorkItems(long total) {
        if (0 == total) {
            this.statusListener.setError("Zero workitems found.");
        }
        this.totalWorkItems = total;
    }

    @Override
    public void setProcessingState(State state) {
        this.state = state;
    }
    
    private InputStream getLocalFileStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(
                "Could not create stream: " + e.getMessage(), e);
        }
    }

    public synchronized StatusMsg getProcessingStatus() {
        return new StatusMsg(successfulResults,
                             unsuccessfulResults,
                             totalWorkItems,
                             state,
                             phase,
                             previousPhaseStatus);
    }

}
