/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.image;

import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.ComputeService;
import org.duracloud.services.image.status.StatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static org.duracloud.services.ComputeService.DELIM;

/**
 * @author: Bill Branan
 * Date: Apr 23, 2010
 */
public class ConversionResultProcessor implements ConversionResultListener {

    private final Logger log = LoggerFactory.getLogger(ConversionResultProcessor.class);
    public static String newline = System.getProperty("line.separator");

    private ContentStore contentStore;
    private StatusListener statusListener;
    private String destSpaceId;

    private int successfulConversions;
    private int unsuccessfulConversions;

    private SimpleDateFormat dateFormat;
    private String resultsId;
    private File resultsFile;
    private String errorsId;
    private File errorsFile;

    public ConversionResultProcessor(ContentStore contentStore,
                                     StatusListener statusListener,
                                     String destSpaceId,
                                     String resultsId,
                                     String errorsId,
                                     File workDir) {
        this.contentStore = contentStore;
        this.statusListener = statusListener;
        this.destSpaceId = destSpaceId;
        this.resultsId = resultsId;
        this.errorsId = errorsId;
        successfulConversions = 0;
        unsuccessfulConversions = 0;

        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        String resultsHeader = getResultHeader();

        resultsFile = new File(workDir, "conversion-results.tsv");
        errorsFile = new File(workDir, "conversion-results.errors.tsv");
        writeToResultsFile(resultsFile, resultsHeader + newline, false);
    }

    private String getResultHeader() {
        return  
            "conversion-date" + DELIM + 
            "source-space-id" + DELIM +
            "content-id" + DELIM + 
            "dest-space-id" + DELIM + 
            "success" + DELIM + 
            "error-message" + DELIM + 
            "conversion-time" + DELIM +
            "total-time" + DELIM + 
            "file size";
    }

    private void writeToResultsFile(File file, String text, boolean append) {
        try {
            FileWriter writer = new FileWriter(file, append);
            writer.append(text);
            writer.close();
        } catch(IOException e) {
            log("Unable to write  (" + text +
                ") to " + file.getAbsolutePath()+": " + e.getMessage());
        }
    }

    public synchronized void  processConversionResult(ConversionResult result) {
        if(result.isSuccess()) {
            successfulConversions++;

        } else {
            unsuccessfulConversions++;
        }

        String line = convertConversionResult(result);
        writeToResultsFile(resultsFile,line, true);
        store(destSpaceId, resultsId, resultsFile);
        
        if(!result.isSuccess()){
            if(!errorsFile.exists()){
                writeToResultsFile(errorsFile,getResultHeader()+newline, true);
            }
            writeToResultsFile(errorsFile,line, true);
            store(destSpaceId, errorsId, errorsFile);
        }
    }

    private void
        store(String spaceId, String contentId, File file) {
        InputStream stream = getResultsStream(file);
        try {
            contentStore.addContent(spaceId,
                                    contentId,
                                    stream,
                                    file.length(),
                                    "text/tab-separated-values",
                                    null,
                                    null);
        } catch(ContentStoreException e) {
            log("Error attempting to store conversion results: " +
                e.getMessage());
        } finally {
            IOUtils.closeQuietly(stream);
        }
        
    }

    private InputStream getResultsStream(File file) {
        try {
            return new FileInputStream(file);
        } catch(FileNotFoundException e) {
            throw new RuntimeException("Could not create stream for "
                + file.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    private String convertConversionResult(ConversionResult result) {
        StringBuffer results = new StringBuffer();
        String date = dateFormat.format(result.getConversionDate());
        results.append(date).append(DELIM);
        results.append(result.getSourceSpaceId()).append(DELIM);
        results.append(result.getContentId()).append(DELIM);
        results.append(result.getDestSpaceId()).append(DELIM);
        results.append(result.isSuccess()).append(DELIM);
        results.append(result.getErrMessage()).append(DELIM);
        results.append(result.getConversionTime()).append(DELIM);
        results.append(result.getTotalTime()).append(DELIM);
        results.append(result.getFileSize());
        results.append(newline);
        return results.toString();
    }

    public String getConversionStatus(boolean conversionComplete) {
        String status = "Conversion In Progress";
        if(conversionComplete) {
            status =  "Conversion Complete";
        }
        return status +
            ". Successful conversions: " + successfulConversions +
            ". Unsuccessful conversions: " + unsuccessfulConversions + ".";
    }

    private void log(String logMsg) {
        log.error(logMsg);
    }

    public Map<String, String> getBubbleableProperties() {
        Map<String,String> props = new HashMap<String,String>();
        props.put(ComputeService.PASS_COUNT_KEY,
                  String.valueOf(this.successfulConversions));
        props.put(ComputeService.FAILURE_COUNT_KEY,
                  String.valueOf(this.unsuccessfulConversions));
        props.put(ComputeService.ITEMS_PROCESS_COUNT,
                  String.valueOf(this.unsuccessfulConversions
                      + this.successfulConversions));
        return props;
    }
}
