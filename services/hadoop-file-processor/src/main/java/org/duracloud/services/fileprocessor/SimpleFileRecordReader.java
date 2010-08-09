/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fileprocessor;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.RecordReader;

import java.io.IOException;

/**
 * Record reader used to provide a set of key/value pairs for each file
 * in a file split. Assumes the file split is a single file and returns
 * the path to the file as the key of a single key/value pair produced
 * per file. 
 *
 * @author: Bill Branan
 * Date: Aug 5, 2010
 */
public class SimpleFileRecordReader implements RecordReader<Text, Text> {
    FileSplit inputSplit;
    String filePath;

    public SimpleFileRecordReader(FileSplit inputSplit) {
        this.inputSplit = inputSplit;
    }

    @Override
    public boolean next(Text key, Text value) throws IOException {
        boolean result = false;

        if(filePath == null) {
            filePath = inputSplit.getPath().toString();

            if(filePath != null) {
                key.set(filePath);
                value.set("");
                result = true;
            }
        }

        if(result) {
            System.out.println("Attempt by record reader to get the next " +
                "record was successful, key=" + key.toString());
        } else {
            System.out.println("Attempt by record reader to get the next " +
                "was not successful");
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