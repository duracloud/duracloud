/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;

/**
 * Record reader, used to provide a set of key/value pairs for each file
 * in a file split. This reader assumes the file split is a single file
 * and creates one key/value pair per file where:
 *   key = the file path
 *   value = the output path (for results)
 *
 * @author: Bill Branan
 * Date: Aug 5, 2010
 */
public class SimpleFileRecordReader implements RecordReader<Text, Text> {

    private FileSplit inputSplit;
    private JobConf jobConf;
    private Reporter reporter;
    private String filePath;

    public SimpleFileRecordReader(FileSplit inputSplit,
                                  JobConf jobConf,
                                  Reporter reporter) {
        this.inputSplit = inputSplit;
        this.jobConf = jobConf;
        this.reporter = reporter;
    }

    @Override
    public boolean next(Text key, Text value) throws IOException {
        boolean result = false;

        if(filePath == null) {
            filePath = inputSplit.getPath().toString();

            System.out.println("Record reader handling file: " + filePath);

            if(filePath != null && !filePath.endsWith("-space-metadata")) {
                key.set(filePath);

                Path outputPath = FileOutputFormat.getOutputPath(jobConf);
                value.set(outputPath.toString());

                result = true;
            }
        }

        if(result) {
            System.out.println("Attempt by record reader to get the next " +
                "record was successful, key=" + key.toString() + ", value=" +
                value.toString());
        } else {
            System.out.println("Attempt by record reader to get the next " +
                "record was not successful");
        }

        return result;
    }

    /**
     * Create an empty Text object in which the key can be stored
     */
    @Override
    public Text createKey() {
        return new Text();
    }

    /**
     * Create an empty Text object in which the value can be stored
     */
    @Override
    public Text createValue() {
        return new Text();
    }

    @Override
    public long getPos() throws IOException {
        if(filePath != null) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void close() throws IOException {
        // Default method body
    }

    @Override
    public float getProgress() throws IOException {
        if(filePath != null) {
            return 1;
        } else {
            return 0;
        }
    }
}