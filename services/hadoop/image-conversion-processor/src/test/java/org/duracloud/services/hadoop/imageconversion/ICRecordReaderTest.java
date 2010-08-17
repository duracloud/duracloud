/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.imageconversion;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Reporter;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: Aug 16, 2010
 */
public class ICRecordReaderTest {

    @Test
    public void testVerifyProcessFile() {
        String inputPath = "file://inputPath";
        JobConf conf = HadoopTestUtil.createJobConf();
        FileSplit split = new FileSplit(new Path(inputPath), 0, 10, conf);

        // Both prefix and suffix set
        conf.set(ICInitParamParser.NAME_PREFIX, "test");
        conf.set(ICInitParamParser.NAME_SUFFIX, ".txt");

        ICRecordReader reader = new ICRecordReader(split, conf, Reporter.NULL);

        assertTrue(reader.verifyProcessFile("test.txt"));
        assertTrue(reader.verifyProcessFile("test123.txt"));
        assertTrue(reader.verifyProcessFile("test-test.txt.txt"));

        assertFalse(reader.verifyProcessFile("test.jpg"));
        assertFalse(reader.verifyProcessFile("foo.txt"));
        assertFalse(reader.verifyProcessFile("my-test.txt"));

        // Only prefix set
        conf.set(ICInitParamParser.NAME_PREFIX, "test");
        conf.set(ICInitParamParser.NAME_SUFFIX, "");

        reader = new ICRecordReader(split, conf, Reporter.NULL);

        assertTrue(reader.verifyProcessFile("test"));
        assertTrue(reader.verifyProcessFile("test.txt"));
        assertTrue(reader.verifyProcessFile("test123.txt"));
        assertTrue(reader.verifyProcessFile("test-test.txt.txt"));
        assertTrue(reader.verifyProcessFile("test.jpg"));

        assertFalse(reader.verifyProcessFile("foo.txt"));
        assertFalse(reader.verifyProcessFile("my-test.txt"));

        // Only suffix set
        conf.set(ICInitParamParser.NAME_PREFIX, "");
        conf.set(ICInitParamParser.NAME_SUFFIX, ".txt");

        reader = new ICRecordReader(split, conf, Reporter.NULL);

        assertTrue(reader.verifyProcessFile("test.txt"));
        assertTrue(reader.verifyProcessFile("test123.txt"));
        assertTrue(reader.verifyProcessFile("test-test.txt.txt"));
        assertTrue(reader.verifyProcessFile("foo.txt"));
        assertTrue(reader.verifyProcessFile("my-test.txt"));

        assertFalse(reader.verifyProcessFile("test.jpg"));
    }

}
