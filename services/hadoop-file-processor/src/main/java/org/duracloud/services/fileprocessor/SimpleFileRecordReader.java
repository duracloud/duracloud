/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fileprocessor;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    private FileSplit inputSplit;
    private JobConf jobConf;
    private Reporter reporter;
    private String tempDir;    
    private String filePath;

    public SimpleFileRecordReader(FileSplit inputSplit,
                                  JobConf jobConf,
                                  Reporter reporter) {
        this.inputSplit = inputSplit;
        this.jobConf = jobConf;
        this.reporter = reporter;

        this.tempDir =
            new File(System.getProperty("java.io.tmpdir")).getAbsolutePath();
    }

    @Override
    public boolean next(Text key, Text value) throws IOException {
        boolean result = false;

        if(filePath == null) {
            filePath = inputSplit.getPath().toString();

            System.out.println("Record reader handling file: " + filePath);

            if(filePath != null) {
                String localPath = moveLocal();
                key.set(localPath);

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

    public String moveLocal() throws IOException {
        Path path = new Path(filePath);
        String fileName = path.getName();

        reporter.setStatus("Moving file to local system for processing: " +
                           fileName);

        FileSystem fs = path.getFileSystem(jobConf);

        if(fs.isFile(path)) {
            // Copy file from remote file system to local storage
            InputStream inputStream = fs.open(path, 2048);
            File localFile = new File(tempDir, fileName);

            System.out.println("Record reader about to read S3 file (" +
                               filePath + ") to local file system " +
                               localFile.getAbsolutePath());

            OutputStream localFileStream = new FileOutputStream(localFile);
            IOUtils.copy(inputStream, localFileStream);

            System.out.println("File moved to local storage successfully");
            return localFile.getAbsolutePath();
        } else {
            System.out.println("Record reader could not retrieve file " +
                               "from S3: " + filePath);
            throw new IOException("Could not retrieve file: " + filePath);
        }
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