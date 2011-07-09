/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.replication.retry;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.hadoop.base.ProcessFileMapper;
import org.duracloud.services.hadoop.base.ProcessResult;
import org.duracloud.services.hadoop.replication.RepMapper;
import org.duracloud.services.hadoop.replication.RepOutputFormat;
import org.duracloud.services.hadoop.store.FileWithMD5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

public class RetryRepMapper extends RepMapper {

    private String result;

    @Override
    public void map(Writable key,
                    Text value,
                    OutputCollector<Text, Text> output,
                    Reporter reporter) throws IOException {
        result = null;
        String line = value.toString();

        if(line.startsWith("failure")) {
            System.out.println("Starting map processing for line: " + line);

            String s3Path = "s3n://";
            int i = line.indexOf(s3Path);
            int j = line.indexOf('\t', i);
            String filePath = line.substring(i, j);
            
            super.map(filePath, null, output, reporter);
        } else {
            System.out.println("Passing result for line: " + line);

            result = line;

            super.collect(output);
        }
    }

    @Override
    protected String collectResult() throws IOException {
        if(result != null) {
            return result;
        }
        return super.collectResult();
    }
}
