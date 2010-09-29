/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.fixity;

import org.duracloud.services.hadoop.base.InitParamParser;
import org.duracloud.services.hadoop.base.JobBuilder;
import org.duracloud.services.hadoop.base.JobRunner;

import java.util.Arrays;
import java.util.Map;

/**
 * This is the main point of entry for the hadoop fixity application.
 *
 * @author: Andrew Woods
 * Date: Sept 21, 2010
 */
public class FixityJobRunner extends JobRunner {

    /**
     * Main method that sets up fixity job.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Fixity Job with args: " + Arrays.toString(
            args));

        JobRunner runner = new FixityJobRunner();

        InitParamParser paramParser = new InitParamParser();
        Map<String, String> initParams = paramParser.parseInitParams(args);

        JobBuilder jobBuilder = new FixityJobBuilder(initParams);
        runner.runJob(jobBuilder);
    }

}
