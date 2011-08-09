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
import org.apache.hadoop.mapred.JobConf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

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

        JobConf conf = HadoopTestUtil.createJobConf();

        AltTextOutputFormat.setOutputPath(conf, new Path(outputDir));
        AltTextOutputFormat outputFormat = new AltTextOutputFormat();

        outputFormat.checkOutputSpecs(null, conf);

        File properties =
            new File(testDir, testDir.getName() + "-space-metadata");
        FileUtils.writeStringToFile(properties, "Test");

        outputFormat.checkOutputSpecs(null, conf);

        File otherFile =
            new File(testDir, "otherfile");
        FileUtils.writeStringToFile(otherFile, "Test");

        outputFormat.checkOutputSpecs(null, conf);
    }
}
