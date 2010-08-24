/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.imageconversion;

import org.duracloud.services.hadoop.base.JobBuilder;

import java.util.Map;

/**
 * This class constructs a hadoop job to perform image processing
 *
 * @author: Bill Branan
 * Date: Aug 13, 2010
 */
public class ICJobBuilder extends JobBuilder {

    /**
     * Constructs an Image Conversion Job builder
     *
     * @param params configuration for the job
     */
    public ICJobBuilder(final Map<String, String> params) {
        super(params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getJobName() {
        return "ImageConversion";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getMapper() {
        return ImageConversionMapper.class;
    }

    /**
     * {@inheritDoc}
     */    
    @Override
    protected Class getInputFormat() {
        return ICInputFormat.class;
    }

    /**
     * {@inheritDoc}
     */    
    @Override
    protected Class getOutputFormat() {
        return ICOutputFormat.class;
    }

}
