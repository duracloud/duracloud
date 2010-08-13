/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;

import java.io.IOException;
import java.util.Map;

/**
 * This is the main point of entry for the hadoop file processing application.
 *
 * @author: Bill Branan
 * Date: Aug 5, 2010
 */
public class JobRunner {

    /**
     * Main method that sets up file processing job.
     */
    public static void main(String[] args) throws Exception {
        JobRunner runner = new JobRunner();

        InitParamParser paramParser = new InitParamParser();
        Map<String, String> initParams = paramParser.parseInitParams(args);

        JobBuilder jobBuilder = new JobBuilder(initParams);
        runner.runJob(jobBuilder);
    }

    /*
     * Construct and run the job.
     */
    public void runJob(JobBuilder jobBuilder)
        throws IOException, java.text.ParseException {

        JobConf jobConf = jobBuilder.getJobConf();
        JobClient.runJob(jobConf);
    }

}
