/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fileprocessor;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;

import java.io.IOException;

/**
 * This is the main point of entry for the file processing hadoop application.
 *
 * @author: Bill Branan
 * Date: Aug 5, 2010
 */
public class FileProcessor {
    
  /**
   * Main method that sets up file processing job.
   */
  public static void main(String[] args) throws IOException,
                                                java.text.ParseException {

    CommandLine cmd = processArgs(args);

    String inputPath = cmd.getOptionValue("input");
    inputPath = appendTrailingSlash(inputPath);

    String outputPath = cmd.getOptionValue("output");
    outputPath = appendTrailingSlash(outputPath);

    runJob(inputPath, outputPath);
  }

  // Construct and run the job.    
  private static void runJob(String inputPath, String outputPath)
      throws IOException, java.text.ParseException {
    JobBuilder jobBuilder = new JobBuilder(inputPath, outputPath);
    JobConf jobConf = jobBuilder.getJobConf();

    System.out.println("Running job to process files.");

    JobClient.runJob(jobConf);
  }

  // Process the command line arguments
  private static CommandLine processArgs(String[] args) {
    Options options = createOptions();
    CommandLine cmd = null;
    try {
      cmd = new PosixParser().parse(options, args);
    } catch (ParseException ex) {
      System.out.println("Error parsing the options");
      printHelpText(options);
    }

    if (!cmd.hasOption("input") || !cmd.hasOption("output")) {
      printHelpText(options);
    }

    return cmd;
  }

  private static String appendTrailingSlash(String inputPath) {
    if (!inputPath.endsWith("/")) {
      inputPath += "/";
    }
    return inputPath;
  }

  private static void printHelpText(Options options) {
    (new HelpFormatter()).printHelp("hadoop jar fileprocessor.jar " +
                                    "-input <path to input> " +
                                    "-output <path to output> ", 
                                    options);
    System.exit(1);      
  }

  private static Options createOptions() {
    Options options = new Options();

    Option inputOption =
       new Option("i", "input", true,
                  "Path to the files that you wish to process " +
                  "e.g s3n://<mybucket>/inputfiles/");
    inputOption.setRequired(true);
    options.addOption(inputOption);

    Option outputOption =
       new Option("o", "output", true,
                  "Path to the location where output files should be written " +
                  "e.g s3n://<mybucket>/outputfiles/");
    outputOption.setRequired(true);
    options.addOption(outputOption);

    return options;
  }

}
