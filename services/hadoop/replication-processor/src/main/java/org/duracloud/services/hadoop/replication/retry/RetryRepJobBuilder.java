/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.replication.retry;

import org.duracloud.services.hadoop.replication.RepJobBuilder;

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
}
