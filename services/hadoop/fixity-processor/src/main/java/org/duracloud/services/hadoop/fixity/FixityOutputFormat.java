/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.fixity;

import org.duracloud.services.hadoop.base.AltTextOutputFormat;

/**
 * @author: Andrew Woods
 * Date: Sept 21, 2010
 */
public class FixityOutputFormat extends AltTextOutputFormat {

    @Override
    protected String getOutputFileName() {
        return "bitIntegrity-bulk/bitIntegrity-results.csv";
    }

}


