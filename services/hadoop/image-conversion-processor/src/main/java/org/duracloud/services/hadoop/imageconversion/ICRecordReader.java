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
import org.duracloud.services.hadoop.base.SimpleFileRecordReader;
import org.duracloud.storage.domain.HadoopTypes;

import static org.duracloud.storage.domain.HadoopTypes.*;

/**
 * @author: Bill Branan
 * Date: Aug 16, 2010
 */
public class ICRecordReader extends SimpleFileRecordReader {

    public ICRecordReader(FileSplit inputSplit,
                          JobConf jobConf,
                          Reporter reporter) {
        super(inputSplit, jobConf, reporter);
    }

    @Override
    protected boolean verifyProcessFile(String filePath) {
        String namePrefix = jobConf.get(TASK_PARAMS.NAME_PREFIX.getLongForm());
        String nameSuffix = jobConf.get(TASK_PARAMS.NAME_SUFFIX.getLongForm());

        String fileName = new Path(filePath).getName();

        boolean prefixVerified = false;
        if (namePrefix == null || namePrefix.equals("")) {
            prefixVerified = true;
        } else if (fileName.startsWith(namePrefix)) {
            prefixVerified = true;
        }

        boolean suffixVerified = false;
        if (nameSuffix == null || nameSuffix.equals("")) {
            suffixVerified = true;
        } else if (fileName.endsWith(nameSuffix)) {
            suffixVerified = true;
        }

        return prefixVerified && suffixVerified;
    }

    @Override
    protected String getOutputPath() {
        String outputSpaceId = jobConf.get(
            TASK_PARAMS.OUTPUT_SPACE_ID.getLongForm());
        return outputSpaceId;
    }

}
