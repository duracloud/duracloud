/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.imageconversion;

import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.duracloud.services.hadoop.base.WholeFileInputFormat;

import java.io.IOException;

/**
 * @author: Bill Branan
 * Date: Aug 16, 2010
 */
public class ICInputFormat extends WholeFileInputFormat {

    @Override
    public RecordReader getRecordReader(InputSplit inputSplit, JobConf jobConf,
                                        Reporter reporter) throws IOException {
        ICRecordReader recordReader =
            new ICRecordReader((FileSplit)inputSplit,
                               jobConf,
                               reporter);
        return recordReader;
    }

}
