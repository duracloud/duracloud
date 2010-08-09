/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fileprocessor;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;

/**
 * Reducer used to collect processing output
 *
 * @author: Bill Branan
 * Date: Aug 5, 2010
 */
public class ResultsReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

  public void reduce(Text key,
                     Iterator<Text> values,
                     OutputCollector<Text, Text> output,
                     Reporter reporter) throws IOException {
    System.out.println("Reducing on key: " + key.toString());  

    while (values.hasNext()) {
      output.collect(key, values.next());
    }
  }
}
