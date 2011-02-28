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
import org.duracloud.storage.domain.HadoopTypes;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

import static org.duracloud.storage.domain.HadoopTypes.*;

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
        conf.set(TASK_PARAMS.NAME_PREFIX.getLongForm(), "test");
        conf.set(TASK_PARAMS.NAME_SUFFIX.getLongForm(), ".txt");

        ICRecordReader reader = new ICRecordReader(split, conf, Reporter.NULL);

        assertTrue(reader.verifyProcessFile("test.txt"));
        assertTrue(reader.verifyProcessFile("test123.txt"));
        assertTrue(reader.verifyProcessFile("test-test.txt.txt"));

        assertFalse(reader.verifyProcessFile("test.jpg"));
        assertFalse(reader.verifyProcessFile("foo.txt"));
        assertFalse(reader.verifyProcessFile("my-test.txt"));

        // Only prefix set
        conf.set(TASK_PARAMS.NAME_PREFIX.getLongForm(), "test");
        conf.set(TASK_PARAMS.NAME_SUFFIX.getLongForm(), "");

        reader = new ICRecordReader(split, conf, Reporter.NULL);

        assertTrue(reader.verifyProcessFile("test"));
        assertTrue(reader.verifyProcessFile("test.txt"));
        assertTrue(reader.verifyProcessFile("test123.txt"));
        assertTrue(reader.verifyProcessFile("test-test.txt.txt"));
        assertTrue(reader.verifyProcessFile("test.jpg"));

        assertFalse(reader.verifyProcessFile("foo.txt"));
        assertFalse(reader.verifyProcessFile("my-test.txt"));

        // Only suffix set
        conf.set(TASK_PARAMS.NAME_PREFIX.getLongForm(), "");
        conf.set(TASK_PARAMS.NAME_SUFFIX.getLongForm(), ".txt");

        reader = new ICRecordReader(split, conf, Reporter.NULL);

        assertTrue(reader.verifyProcessFile("test.txt"));
        assertTrue(reader.verifyProcessFile("test123.txt"));
        assertTrue(reader.verifyProcessFile("test-test.txt.txt"));
        assertTrue(reader.verifyProcessFile("foo.txt"));
        assertTrue(reader.verifyProcessFile("my-test.txt"));

        assertFalse(reader.verifyProcessFile("test.jpg"));
    }

    @Test
    public void testGetOutputPath() {
        String inputPath = "file://inputPath";
        JobConf conf = HadoopTestUtil.createJobConf();
        FileSplit split = new FileSplit(new Path(inputPath), 0, 10, conf);

        conf.set(TASK_PARAMS.OUTPUT_SPACE_ID.getLongForm(), "test");

        ICRecordReader reader = new ICRecordReader(split, conf, Reporter.NULL);
        assertEquals("test", reader.getOutputPath());
    }

}
