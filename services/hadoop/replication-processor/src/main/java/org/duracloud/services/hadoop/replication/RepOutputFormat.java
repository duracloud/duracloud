/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.replication;

import org.duracloud.services.hadoop.base.AltTextOutputFormat;

/**
 * @author: Bill Branan
 * Date: Sept 23, 2010
 */
public class RepOutputFormat extends AltTextOutputFormat {

    @Override
    public String getOutputFileName() {
        return "duplicate-on-demand/duplicate-results.tsv";
    }

}