/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fileprocessor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.s3.S3FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * Mapper used to process files.
 *
 * @author: Bill Branan
 * Date: Aug 5, 2010
 */
public class ProcessFileMapper extends MapReduceBase
	implements Mapper<Text, Text, Text, Text>
{
    private static String outputPath;

    private String tempDir;

    public ProcessFileMapper() {
        this.tempDir =
            new File(System.getProperty("java.io.tmpdir")).getAbsolutePath();
    }

    public static void setOutputPath(String path) {
        outputPath = path;
    }

    @Override
    public void map(Text key,
                    Text value,
                    OutputCollector<Text, Text> output,
                    Reporter reporter) throws IOException {
        String filePath = key.toString();
        reporter.setStatus("Retrieving file from S3: " + filePath);

        System.out.println("Beginning map process, retrieving file from S3: " +
                           filePath);        

        Path file = new Path(filePath);
        String fileName = file.getName();

        S3FileSystem s3 = new S3FileSystem();
        s3.initialize(URI.create(filePath), new JobConf());
        
        if(s3.isFile(file)) {
            // Copy file from S3 to local storage
            InputStream inputStream = s3.open(file, 2048);
            File localFile = new File(tempDir, fileName);
            OutputStream localFileStream = new FileOutputStream(localFile);
            IOUtils.copy(inputStream, localFileStream);

            reporter.setStatus("Processing file: " + fileName);

            System.out.println("Continuing map process, file moved to local " +
                               "storage successfully, performing processing");

            // Process the file
            // TODO: make this more generic

            if(outputPath != null) {
                Path outputFile = new Path(outputPath, fileName);
                FSDataOutputStream outputStream = s3.create(outputFile);

                // Test implementation to be replaced by real processing
                String outputText = "Processed local file: " +
                                    localFile.getAbsolutePath() +
                                    " in ProcessFileMapper";
                InputStream textStream =
                    new ByteArrayInputStream(outputText.getBytes("UTF-8"));
                IOUtils.copy(textStream, outputStream);
                // End test implementation
            } else {
                System.out.println("Output path is null, not able to complete " +
                    "processing of local file:" + localFile.getAbsolutePath());
            }

            FileUtils.deleteQuietly(localFile);

            output.collect(new Text(filePath), new Text("success"));

            System.out.println("Finished map process, file processing complete");
        } else {
            output.collect(new Text(filePath), new Text("failure"));

            System.out.println("Map processing failed, file not found in S3");
        }

        reporter.setStatus("Processing complete for file: " + fileName);
    }

}
