/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.replication;

import org.duracloud.services.hadoop.base.InitParamParser;
import org.duracloud.services.hadoop.base.JobBuilder;
import org.duracloud.services.hadoop.base.JobRunner;

import java.util.Arrays;
import java.util.Map;

/**
 * This is the main point of entry for the hadoop replication
 * application.
 *
 * @author: Bill Branan
 * Date: Sept 23, 2010
 */
public class RepJobRunner extends JobRunner {

    /**
     * Main method that sets up replication job.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Replication Job with args: " +
                           Arrays.toString(args));

        JobRunner runner = new RepJobRunner();

        InitParamParser paramParser = new RepInitParamParser();
        Map<String, String> initParams = paramParser.parseInitParams(args);

        JobBuilder jobBuilder = new RepJobBuilder(initParams);
        runner.runJob(jobBuilder);
    }

}
