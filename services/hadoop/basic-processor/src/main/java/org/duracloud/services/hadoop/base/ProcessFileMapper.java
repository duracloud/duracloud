/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;

/**
 * Mapper used to process files.
 *
 * @author: Bill Branan
 * Date: Aug 5, 2010
 */
public class ProcessFileMapper extends MapReduceBase implements Mapper<Text, Text, Text, Text> {

    @Override
    public void map(Text key,
                    Text value,
                    OutputCollector<Text, Text> output,
                    Reporter reporter) throws IOException {
        System.out.println("Mapper Processing File: " + key.toString());
    }

}
