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

    public ConversionResultProcessor(ContentStore contentStore,
                                     StatusListener statusListener,
                                     String destSpaceId,
                                     String resultsId,
                                     File workDir) {
        this.contentStore = contentStore;
        this.statusListener = statusListener;
        this.destSpaceId = destSpaceId;
        this.resultsId = resultsId;
        successfulConversions = 0;
        unsuccessfulConversions = 0;

        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        String resultsHeader =
            "conversion-date" + DELIM + "source-space-id" + DELIM +
                "content-id" + DELIM + "dest-space-id" + DELIM + "success" +
                DELIM + "error-message" + DELIM + "conversion-time" + DELIM +
                "total-time" + DELIM + "file size";

        resultsFile = new File(workDir, "conversion-results.csv");
        writeToResultsFile(resultsHeader + newline, false);
    }

    private void writeToResultsFile(String text, boolean append) {
        try {
            FileWriter writer = new FileWriter(resultsFile, append);
            writer.append(text);
            writer.close();
        } catch(IOException e) {
            log("Unable to write result (" + text +
                ") to results file: " + e.getMessage());
        }
    }

    public synchronized void  processConversionResult(ConversionResult result) {
        if(result.isSuccess()) {
            successfulConversions++;

        } else {
            unsuccessfulConversions++;

            String error = result.getErrMessage();
            if (error == null) {
                error = result.getDestSpaceId() + ":" + result.getContentId();
            }
            statusListener.setError(error);
        }

        writeToResultsFile(convertConversionResult(result), true);

        InputStream resultsStream = getResultsStream();
        try {
            contentStore.addContent(destSpaceId,
                                    resultsId,
                                    resultsStream,
                                    resultsFile.length(),
                                    "text/csv",
                                    null,
                                    null);
        } catch(ContentStoreException e) {
            log("Error attempting to store conversion results: " +
                e.getMessage());
        } finally {
            IOUtils.closeQuietly(resultsStream);
        }
    }

    private InputStream getResultsStream() {
        try {
            return new FileInputStream(resultsFile);
        } catch(FileNotFoundException e) {
            throw new RuntimeException("Could not create results stream: " +
                                       e.getMessage(), e);
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
}
