/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import org.duracloud.retrieval.source.ContentItem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles writing the output CSV file for the retrieval tool
 *
 * @author: Bill Branan
 * Date: Oct 13, 2010
 */
public class OutputWriter {

    private static final DateFormat DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";

    private PrintWriter writer;

    public OutputWriter(File workDir) {
        DateFormat nameFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String writerFileName =
            "retrieval-tool-output-" + nameFormat.format(new Date()) + ".csv";
        File writerFile = new File(workDir, writerFileName);

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(writerFile)));
        } catch(IOException e) {
            throw new RuntimeException("Unable to initialize output writer " +
                                       "due to: " + e.getMessage());    
        }

        writeHeader();
    }

    private void writeHeader() {
        writer.println("result, spaceId, contentId, local file, error, date");
        writer.flush();
    }

    public void writeSuccess(ContentItem contentItem, String localFilePath) {
        StringBuilder result = new StringBuilder();
        result.append(SUCCESS).append(",");
        result.append(contentItem.getSpaceId()).append(",");
        result.append(quote(contentItem.getContentId())).append(",");
        result.append(quote(localFilePath)).append(",");
        result.append("none").append(",");
        result.append(DATE_FORMAT.format(new Date()));
        writer.println(result.toString());
        writer.flush();
    }

    public void writeFailure(ContentItem contentItem, String error) {
        StringBuilder result = new StringBuilder();
        result.append(FAILURE).append(",");
        result.append(contentItem.getSpaceId()).append(",");
        result.append(quote(contentItem.getContentId())).append(",");
        result.append("none").append(",");
        result.append(quote(error)).append(",");
        result.append(DATE_FORMAT.format(new Date()));
        writer.println(result.toString());
        writer.flush();
    }

    private String quote(String toCheck) {
        if(toCheck.indexOf(",") > -1) {
            return "\"" + toCheck + "\"";
        } else {
            return toCheck;
        }
    }

    public void close() {
        writer.close();
    }

}
