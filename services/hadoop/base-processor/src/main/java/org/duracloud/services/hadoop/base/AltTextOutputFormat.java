/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.hadoop.fs.ContentSummary;
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
 * include a DuraCloud space metadata file.
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

    @Override
    public void checkOutputSpecs(FileSystem ignored, JobConf job)
        throws IOException {
        try {
            super.checkOutputSpecs(ignored, job);
        } catch(FileAlreadyExistsException e) {
            Path outDir = getOutputPath(job);
            FileSystem fs = outDir.getFileSystem(job);

            if (fs.exists(outDir)) {
                ContentSummary contentSummary =
                    fs.getContentSummary(outDir);
                long fileCount = contentSummary.getFileCount();

                if(fileCount == 0) {
                    return;
                } else if(fileCount == 1) {
                    String spaceName = outDir.toUri().getAuthority();
                    if(spaceName == null || spaceName.equals("")) {
                        spaceName = outDir.getName();
                    }

                    String spaceMetaName = spaceName + "-space-metadata";
                    Path spaceMetaPath = new Path(outDir, spaceMetaName);

                    if(fs.exists(spaceMetaPath)) {
                        if(fs.isFile(spaceMetaPath)) {
                            return;
                        }
                    }
                } else if (fileCount > 1) {
                    String error = "Output bucket must be empty (except " +
                        "for space metadata file) to ensure that content " +
                        "is not overwritten, there are " + fileCount +
                        " files in this bucket";
                    throw new FileAlreadyExistsException(error);
                }
            }

            // Cause for exception not obvious, re-throw the original exception
            throw e;
        }
    }
}
