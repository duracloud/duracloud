/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.duracloud.services.hadoop.base.JobRunner;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * @author: Bill Branan
 * Date: Aug 11, 2010
 */
public class JobRunnerTest {

    @Test
    public void testProcessArgs() throws Exception {
        JobRunner jobRunner = new JobRunner();
        try {
            jobRunner.processArgs(null);
            fail("Job Runner should fail when no arguments are provided");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        String[] args = {"-input", "inputFile", "-output", "outputFile"};
        jobRunner.processArgs(args);

        String[] argsShort = {"-i", "inputFile", "-o", "outputFile"};
        jobRunner.processArgs(argsShort);
    }

}
