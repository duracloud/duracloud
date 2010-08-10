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
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Mapper used to process files.
 *
 * @author: Bill Branan
 * Date: Aug 5, 2010
 */
public class ProcessFileMapper extends MapReduceBase
	implements Mapper<Text, Text, Text, Text>
{
    @Override
    public void map(Text key,
                    Text value,
                    OutputCollector<Text, Text> output,
                    Reporter reporter) throws IOException {
        String localFilePath = key.toString();
        String outputPath = value.toString();

        reporter.setStatus("Processing file: " + localFilePath);
        System.out.println("Beginning map process, processing file: " +
                           localFilePath + ". Output path: " + outputPath);

        File localFile = new File(localFilePath);

        if(localFile.exists()) {
            String fileName = localFile.getName();

            InputStream resultStream = processFile(localFile);

            System.out.println("File processing complete for file " + fileName +
                               ", moving result to output location");

            copyToOutput(resultStream, fileName, outputPath);

            FileUtils.deleteQuietly(localFile);

            output.collect(new Text(fileName), new Text("success"));

            System.out.println("Map processing completed for: " + fileName);
        } else {
            output.collect(new Text(localFilePath), new Text("failure"));

            System.out.println("Map processing failed for " + localFilePath +
                               ". File not found");
        }

        reporter.setStatus("Processing complete for file: " + localFilePath);
    }

    private InputStream processFile(File file) throws IOException {
        // Test implementation to be replaced by real processing
        String outputText = "Processed local file: " + file.getAbsolutePath() +
                            " in ProcessFileMapper";
        return new ByteArrayInputStream(outputText.getBytes("UTF-8"));
    }

    private void copyToOutput(InputStream resultStream,
                              String fileName,
                              String outputPath) throws IOException {
            if(outputPath != null) {
                Path outputFile = new Path(outputPath, fileName);
                FileSystem outputFS = outputFile.getFileSystem(new JobConf());
                FSDataOutputStream outputStream = outputFS.create(outputFile);

                IOUtils.copy(resultStream, outputStream);
            } else {
                System.out.println("Output path is null, not able to " +
                                   "store result of processing local file");
            }
    }

}
