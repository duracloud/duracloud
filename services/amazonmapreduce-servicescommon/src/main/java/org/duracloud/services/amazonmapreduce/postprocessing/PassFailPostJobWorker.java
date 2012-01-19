/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.postprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.client.util.ContentStoreUtil;
import org.duracloud.client.util.DuracloudFileWriter;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.services.ComputeService;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReducePostJobWorker;
import org.duracloud.services.amazonmapreduce.util.ContentStreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This PostJobWorker examines the final report of a given service run and
 * pushes any errors found up to the parent service.
 * Implementations of this class need to provide the logic to determine if a
 * given line from the final report contains an error or not.
 *
 * @author Andrew Woods
 *         Date: 6/8/11
 */
public abstract class PassFailPostJobWorker extends BaseAmazonMapReducePostJobWorker {
    private static final String PASS_COUNT = ComputeService.PASS_COUNT_KEY;
    private static final String FAILURE_COUNT = ComputeService.FAILURE_COUNT_KEY;
    private static final String ITEMS_PROCESSED_COUNT = ComputeService.ITEMS_PROCESS_COUNT;
    private static Logger log = LoggerFactory.getLogger(PassFailPostJobWorker.class);
    private ContentStoreUtil storeUtil;
    private ContentStore contentStore;
    private ContentStreamUtil streamUtil;
    private String serviceWorkDir;
    private String spaceId;
    private String contentId; //contentId of service output (ie report)
    private String errorReportContentId;
    private long totalItemsProcessedCount = -1;
    private long failedItemsCount = -1;
    
    public PassFailPostJobWorker(AmazonMapReduceJobWorker predecessor,
                                 ContentStore contentStore,
                                 String serviceWorkDir,
                                 String spaceId,
                                 String contentId, 
                                 String errorReportContentId) {
        super(predecessor);
        ContentStreamUtil util = new ContentStreamUtil();
        init(contentStore, util, serviceWorkDir, spaceId, contentId, errorReportContentId);
    }

    public PassFailPostJobWorker(AmazonMapReduceJobWorker predecessor,
                                 ContentStore contentStore,
                                 ContentStreamUtil streamUtil,
                                 String serviceWorkDir,
                                 String spaceId,
                                 String contentId,
                                 String errorReportContentId,
                                 long sleepMillis) {
        super(predecessor, sleepMillis);
        init(contentStore, streamUtil, serviceWorkDir, spaceId, contentId, errorReportContentId);
    }

    private void init(ContentStore contentStore,
                      ContentStreamUtil streamUtil,
                      String serviceWorkDir,
                      String spaceId,
                      String contentId,
                      String errorReportContentId) {
        
        this.contentStore = contentStore;
        this.storeUtil = new ContentStoreUtil(contentStore);
        this.streamUtil = streamUtil;

        this.serviceWorkDir = serviceWorkDir;
        this.spaceId = spaceId;
        this.contentId = contentId;
        this.errorReportContentId = errorReportContentId;
    }

    @Override
    protected void doWork() {


        this.failedItemsCount = 0;

        BufferedReader reader = getFileReader(getCachedContent());

        // skip header line
        String header = readLine(reader);

        DuracloudFileWriter errorReportWriter =  null;
        String line = null;
        long count = 0;
        boolean completeFailure = false;
        
        while ((line = readLine(reader)) != null) {
            if(line.trim().length() == 0){
                continue;
            }
            if(count == 0 && isCompleteFailure(line)){
                completeFailure = true;
                setError(line);
                break;
            }
            if (isError(line)) {
                try {
                    if(errorReportWriter == null){
                        errorReportWriter = new DuracloudFileWriter(spaceId, errorReportContentId, "text/tab-separated-values", contentStore);
                        errorReportWriter.writeLine(header);
                    }
                    
                    errorReportWriter.writeLine(line);
                } catch (IOException e) {
                    log.error("failed to write to error report [" + errorReportContentId + "]", e);
                    e.printStackTrace();
                }
                
                failedItemsCount++;
            }
            
            count++;
        }

        IOUtils.closeQuietly(reader);

        if(!completeFailure){
            this.totalItemsProcessedCount = count;
        }

        if (this.failedItemsCount > 0) {
            if(errorReportWriter != null){
                IOUtils.closeQuietly(errorReportWriter);
            }
        }
    }

    protected boolean isCompleteFailure(String line) {
        return false;
    }

    protected abstract boolean isError(String line);

    private BufferedReader getFileReader(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {

        }
        return reader;
    }


    private File getCachedContent() {
        InputStream input = storeUtil.getContentStream(spaceId, contentId);

        File file = new File(serviceWorkDir, contentId);
        OutputStream output = streamUtil.createOutputStream(file);

        streamUtil.writeToOutputStream(input, output);

        IOUtils.closeQuietly(output);
        IOUtils.closeQuietly(input);
        return file;
    }

    private String readLine(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new DuraCloudRuntimeException("Error reading line", e);
        }
    }
    
    @Override
    public Map<String, String> getBubbleableProperties() {
        Map<String,String> props =  super.getBubbleableProperties();
        if(this.totalItemsProcessedCount > -1){
            props.put(ITEMS_PROCESSED_COUNT,String.valueOf(this.totalItemsProcessedCount));
            props.put(PASS_COUNT,String.valueOf(this.totalItemsProcessedCount-this.failedItemsCount));
            props.put(FAILURE_COUNT,String.valueOf(this.failedItemsCount));
        }

        return props;

    }
    
}
