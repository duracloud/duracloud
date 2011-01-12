/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.imageconversion;

import org.duracloud.services.hadoop.base.AltTextOutputFormat;

/**
 * @author: Bill Branan
 * Date: Aug 24, 2010
 */
public class ICOutputFormat extends AltTextOutputFormat {

    @Override
    protected String getOutputFileName() {
        return "imageTransformer-bulk/imageTransformer-results.csv";
    }

}


