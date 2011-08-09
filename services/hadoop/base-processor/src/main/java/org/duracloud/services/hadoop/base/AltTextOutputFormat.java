/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileAlreadyExistsException;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.Progressable;

import java.io.IOException;

/**
 * Overrides the text output format to allow the output directory to exist and
 * include a DuraCloud space properties file.
 *
 * @author: Bill Branan
 * Date: Aug 12, 2010
 */
public class AltTextOutputFormat<K, V> extends TextOutputFormat<K, V> {

    @Override
    public RecordWriter<K, V> getRecordWriter(FileSystem ignored,
                                              JobConf job,
                                              String name,
                                              Progressable progress)
        throws IOException {
        String newName = getOutputFileName();
        return super.getRecordWriter(ignored, job, newName, progress);
    }

    protected String getOutputFileName() {
        return "duracloud-service-results";
    }

    /**
     * Allows output directory (where results file will be stored) to already
     * exist and to contain other files.
     */
    @Override
    public void checkOutputSpecs(FileSystem ignored, JobConf job)
        throws IOException {
        try {
            super.checkOutputSpecs(ignored, job);
        } catch(FileAlreadyExistsException e) {
            Path outDir = getOutputPath(job);
            FileSystem fs = outDir.getFileSystem(job);

            if (fs.exists(outDir)) {
                return;
            }

            // Cause for exception not obvious, re-throw the original exception
            throw e;
        }
    }
}
