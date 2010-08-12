/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileAlreadyExistsException;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * @author: Bill Branan
 * Date: Aug 12, 2010
 */
public class AltTextOutputFormatTest {

    File testDir;

    @Before
    public void setUp() throws Exception {
        testDir = new File("target/testing-dir");
        testDir.mkdir();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(testDir);
    }

    @Test
    public void testCheckOutputSpecs() throws Exception {
        String outputDir =
            "file:///" + testDir.getAbsolutePath().replaceAll("\\\\", "/");

        // An unnecessary stack track is printed when creating a JobConf
        // See org.apache.hadoop.conf.Configuration line 211
        System.out.println("--- BEGIN EXPECTED STACK TRACE ---");
        JobConf conf = new JobConf();
        System.out.println("--- END EXPECTED STACK TRACE ---");

        AltTextOutputFormat.setOutputPath(conf, new Path(outputDir));
        AltTextOutputFormat outputFormat = new AltTextOutputFormat();

        outputFormat.checkOutputSpecs(null, conf);

        File metadata =
            new File(testDir, testDir.getName() + "-space-metadata");
        FileUtils.writeStringToFile(metadata, "Test");

        outputFormat.checkOutputSpecs(null, conf);

        File otherFile =
            new File(testDir, "otherfile");
        FileUtils.writeStringToFile(otherFile, "Test");

        try {
            outputFormat.checkOutputSpecs(null, conf);
            fail("Exception expected");
        } catch(FileAlreadyExistsException expected) {
            assertNotNull(expected);
        }

    }
}
