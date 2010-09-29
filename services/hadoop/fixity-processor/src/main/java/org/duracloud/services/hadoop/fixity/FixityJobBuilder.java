/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.fixity;

import org.duracloud.services.hadoop.base.JobBuilder;

import java.util.Map;

/**
 * This class constructs a hadoop job to perform fixity service.
 *
 * @author: Andrew Woods
 * Date: Sept 21, 2010
 */
public class FixityJobBuilder extends JobBuilder {

    /**
     * Constructs a Fixity Job builder
     *
     * @param params configuration for the job
     */
    public FixityJobBuilder(final Map<String, String> params) {
        super(params);
    }

    @Override
    protected String getJobName() {
        return "Fixity";
    }

    @Override
    protected Class getMapper() {
        return HashFinderMapper.class;
    }

    @Override
    protected Class getOutputFormat() {
        return FixityOutputFormat.class;
    }

}
