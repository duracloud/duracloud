/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.duracloud.common.model.ContentItem;

/**
 * Handles writing the output CSV file for the retrieval tool
 *
 * @author: Bill Branan
 * Date: Oct 13, 2010
 */
public class CSVFileOutputWriter implements OutputWriter {

    protected static final String SUCCESS = "success";
    protected static final String FAILURE = "failure";
    protected static final String MISSING = "missing";
    protected static final String OUTPUT_PREFIX = "retrieval-tool-output-";

    private PrintWriter writer;

    public CSVFileOutputWriter(File workDir) {
        DateFormat nameFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String writerFileName =
            OUTPUT_PREFIX + nameFormat.format(new Date()) + ".csv";
        File writerFile = new File(workDir, writerFileName);

        try {
            writer =
                new PrintWriter(new BufferedWriter(new FileWriter(writerFile)));
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize output writer " +
                                       "due to: " + e.getMessage());
        }

        writeHeader();
    }

    private void writeHeader() {
        writer.println("result, spaceId, contentId, " +
                       "local file, error, attempts, date");
        writer.flush();
    }

    @Override
    public void writeSuccess(ContentItem contentItem,
                             String localFilePath,
                             int attempts) {
        StringBuilder result = new StringBuilder();
        result.append(SUCCESS).append(",");
        result.append(contentItem.getSpaceId()).append(",");
        result.append(quote(contentItem.getContentId())).append(",");
        result.append(quote(localFilePath)).append(",");
        result.append("none").append(",");
        result.append(attempts).append(",");
        result.append(DATE_FORMAT.format(new Date()));
        writer.println(result.toString());
        writer.flush();
    }

    @Override
    public void writeFailure(ContentItem contentItem,
                             String error,
                             int attempts) {
        StringBuilder result = new StringBuilder();
        result.append(FAILURE).append(",");
        result.append(contentItem.getSpaceId()).append(",");
        result.append(quote(contentItem.getContentId())).append(",");
        result.append("none").append(",");
        result.append(quote(error)).append(",");
        result.append(attempts).append(",");
        result.append(DATE_FORMAT.format(new Date()));
        writer.println(result.toString());
        writer.flush();
    }

    @Override
    public void writeMissing(ContentItem contentItem,
                             String message,
                             int attempts) {
        StringBuilder result = new StringBuilder();
        result.append(MISSING).append(",");
        result.append(contentItem.getSpaceId()).append(",");
        result.append(quote(contentItem.getContentId())).append(",");
        result.append("none").append(",");
        result.append(quote(message)).append(",");
        result.append(attempts).append(",");
        result.append(DATE_FORMAT.format(new Date()));
        writer.println(result.toString());
        writer.flush();
    }

    private String quote(String toCheck) {
        if (toCheck.indexOf(",") > -1) {
            return "\"" + toCheck + "\"";
        } else {
            return toCheck;
        }
    }

    @Override
    public void close() {
        writer.close();
    }

}
