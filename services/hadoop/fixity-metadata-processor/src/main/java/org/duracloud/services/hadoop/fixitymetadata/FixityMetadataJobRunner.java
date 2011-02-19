/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.fixitymetadata;

import org.duracloud.services.hadoop.base.InitParamParser;
import org.duracloud.services.hadoop.base.JobBuilder;
import org.duracloud.services.hadoop.base.JobRunner;

import java.util.Arrays;
import java.util.Map;

/**
 * This is the main point of entry for the hadoop fixity application.
 *
 * @author: Andrew Woods
 * Date: Feb 9, 2011
 */
public class FixityMetadataJobRunner extends JobRunner {

    /**
     * Main method that sets up fixity job.
     */
    public static void main(String[] args) throws Exception {
        System.out.println(
            "Starting Fixity Metadata Job with args: " + Arrays.toString(args));

        JobRunner runner = new FixityMetadataJobRunner();

        InitParamParser paramParser = new InitParamParser();
        Map<String, String> initParams = paramParser.parseInitParams(args);

        JobBuilder jobBuilder = new FixityMetadataJobBuilder(initParams);
        runner.runJob(jobBuilder);
    }

}
