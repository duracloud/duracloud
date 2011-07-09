/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.replication.retry;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.duracloud.services.hadoop.base.AltTextOutputFormat;
import org.duracloud.services.hadoop.replication.RepJobBuilder;
import org.duracloud.services.hadoop.replication.RepOutputFormat;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

public class RetryRepJobBuilder extends RepJobBuilder {

    /**
     * Constructs a Replication Job builder
     *
     * @param params configuration for the job
     */
    public RetryRepJobBuilder(final Map<String, String> params) {
        super(params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobConf getJobConf() throws IOException, ParseException {
        JobConf conf = super.getJobConf();

        RepOutputFormat outputFormat = new RepOutputFormat();

        TextInputFormat.setInputPaths(conf,
                                              new Path(AltTextOutputFormat.getOutputPath(conf),
                                                       outputFormat.getOutputFileName()));
        return conf;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getJobName() {
        return "RetryReplication";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getMapper() {
        return RetryRepMapper.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getInputFormat() {
        return TextInputFormat.class;
    }
}
