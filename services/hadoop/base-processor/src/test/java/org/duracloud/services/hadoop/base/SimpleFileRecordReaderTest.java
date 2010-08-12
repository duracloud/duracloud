/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.duracloud.services.hadoop.base.SimpleFileRecordReader;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: Aug 11, 2010
 */
public class SimpleFileRecordReaderTest {

    @Test
    public void testSimpleFileRecordReader() throws Exception {
        String inputPath = "file://inputPath";
        String outputPath = "file://outputPath";

        // An unnecessary stack track is printed when creating a JobConf
        // See org.apache.hadoop.conf.Configuration line 211
        System.out.println("--- BEGIN EXPECTED STACK TRACE ---");
        JobConf conf = new JobConf();
        System.out.println("--- END EXPECTED STACK TRACE ---");

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);
        FileOutputFormat.setOutputPath(conf, new Path(outputPath));
        conf.setOutputFormat(TextOutputFormat.class);

        FileSplit split = new FileSplit(new Path(inputPath), 0, 10, conf);        

        SimpleFileRecordReader reader =
            new SimpleFileRecordReader(split, conf, Reporter.NULL);

        Text key = reader.createKey();
        Text value = reader.createValue();

        assertNotNull(key);
        assertNotNull(value);

        assertEquals(0, reader.getPos());
        assertEquals(Float.valueOf(0), reader.getProgress());

        reader.next(key, value);

        assertEquals(inputPath, key.toString());
        assertEquals(outputPath, value.toString());

        assertEquals(1, reader.getPos());
        assertEquals(Float.valueOf(1), reader.getProgress());
    }
}
