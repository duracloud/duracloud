/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.replication;

import org.duracloud.services.hadoop.base.JobBuilder;

import java.util.Map;

/**
 * This class constructs a hadoop job to perform replication
 *
 * @author: Bill Branan
 * Date: Sept 23, 2010
 */
public class RepJobBuilder extends JobBuilder {

    /**
     * Constructs a Replication Job builder
     *
     * @param params configuration for the job
     */
    public RepJobBuilder(final Map<String, String> params) {
        super(params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getJobName() {
        return "Replication";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getMapper() {
        return RepMapper.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getOutputFormat() {
        return RepOutputFormat.class;
    }

}
