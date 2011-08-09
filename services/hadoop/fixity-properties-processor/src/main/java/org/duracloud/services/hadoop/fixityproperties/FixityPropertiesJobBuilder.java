/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.fixityproperties;

import org.duracloud.services.hadoop.base.JobBuilder;

import java.util.Map;

/**
 * This class constructs a hadoop job to perform fixity service.
 *
 * @author: Andrew Woods
 * Date: Feb 9, 2011
 */
public class FixityPropertiesJobBuilder extends JobBuilder {

    /**
     * Constructs a Fixity Job builder
     *
     * @param params configuration for the job
     */
    public FixityPropertiesJobBuilder(final Map<String, String> params) {
        super(params);
    }

    @Override
    protected String getJobName() {
        return "FixityProperties";
    }

    @Override
    protected Class getMapper() {
        return HashPropertiesFinderMapper.class;
    }

    @Override
    protected Class getOutputFormat() {
        return FixityPropertiesOutputFormat.class;
    }

}
