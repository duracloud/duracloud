/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * @author: Bill Branan
 * Date: Aug 11, 2010
 */
public class JobBuilderTest {

    @Test
    public void testJobBuilder() throws Exception {
        String inputPath = "file://inputPath";
        String outputPath = "file://outputPath";

        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(InitParamParser.INPUT_PATH, inputPath);
        initParams.put(InitParamParser.OUTPUT_PATH, outputPath);

        String testParamName = "testName";
        String testParamValue = "testValue";
        initParams.put(testParamName, testParamValue);

        JobBuilder jobBuilder = new JobBuilder(initParams);
        assertEquals("ProcessFiles", jobBuilder.getJobName());
        assertEquals(ProcessFileMapper.class, jobBuilder.getMapper());
        assertEquals(ResultsReducer.class, jobBuilder.getReducer());

        // An unnecessary stack track is printed when creating a JobConf
        // See org.apache.hadoop.conf.Configuration line 211
        System.out.println("--- BEGIN EXPECTED STACK TRACE ---");
        JobConf jobConf = jobBuilder.getJobConf();
        System.out.println("--- END EXPECTED STACK TRACE ---");

        Path[] paths = FileInputFormat.getInputPaths(jobConf);
        assertEquals(1, paths.length);
        assertEquals(inputPath + "/", paths[0].toString());

        assertEquals(outputPath + "/",
                     FileOutputFormat.getOutputPath(jobConf).toString());

        assertEquals(testParamValue, jobConf.get(testParamName));
    }

}
