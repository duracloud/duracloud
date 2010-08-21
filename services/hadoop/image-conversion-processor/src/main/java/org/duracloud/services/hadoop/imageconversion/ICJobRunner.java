/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.imageconversion;

import org.duracloud.services.hadoop.base.InitParamParser;
import org.duracloud.services.hadoop.base.JobBuilder;
import org.duracloud.services.hadoop.base.JobRunner;

import java.util.Arrays;
import java.util.Map;

/**
 * This is the main point of entry for the hadoop image 
 * conversion application.
 *
 * @author: Bill Branan
 * Date: Aug 13, 2010
 */
public class ICJobRunner extends JobRunner {

    /**
     * Main method that sets up image conversion job.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Image Conversion Job with args: " +
                           Arrays.toString(args));  

        JobRunner runner = new ICJobRunner();
        
        InitParamParser paramParser = new ICInitParamParser();
        Map<String, String> initParams = paramParser.parseInitParams(args);

        JobBuilder jobBuilder = new ICJobBuilder(initParams);
        runner.runJob(jobBuilder);
    }

}
