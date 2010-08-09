/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fileprocessor;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextOutputFormat;

import java.io.IOException;
import java.text.ParseException;

/**
 * This class constructs a job.
 *
 * @author: Bill Branan
 * Date: Aug 5, 2010
 */
public class JobBuilder {

  private String inputPathPrefix;
  private String outputPath;

  /**
   * Constructs a Job builder
   * 
   * @param inputPathPrefix
   *          The S3 path from which the input files can be retrieved.
   * @param outputPath
   *          The S3 path to which output files will be written.
   */
  public JobBuilder(final String inputPathPrefix, final String outputPath) {
    this.inputPathPrefix = inputPathPrefix;
    this.outputPath = outputPath;
  }

  /**
   * Constructs the JobConf to be used to run the map reduce job.
   */
  public JobConf getJobConf() throws IOException, ParseException {

    System.out.println("Creating job to process files in " + inputPathPrefix + 
                       " and store results in " + outputPath);

    JobConf conf = new JobConf(JobBuilder.class);
    conf.setJobName("ProcessFiles");
    conf.setOutputKeyClass(Text.class);
    conf.setOutputValueClass(Text.class);

    // Configure mappper
    ProcessFileMapper.setOutputPath(outputPath);
    conf.setMapperClass(ProcessFileMapper.class);

    // Configure reducer
    conf.setReducerClass(ResultsReducer.class);
    conf.setNumReduceTasks(1);

    // Configure input path
    WholeFileInputFormat.addInputPath(conf, new Path(inputPathPrefix));
    conf.setInputFormat(WholeFileInputFormat.class);

    // Configure output path
    FileOutputFormat.setOutputPath(conf, new Path(outputPath));
    conf.setOutputFormat(TextOutputFormat.class);

    // Other config
    conf.setCompressMapOutput(false);

    return conf;
  }

}
