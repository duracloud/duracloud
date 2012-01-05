/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce;

import java.util.Map;

/**
 * This interface defines additional functionality specific to post processing workers.
 * @author Daniel Bernstein
 *         Date: December 30, 2011
 *
 */
public interface AmazonMapReducePostJobWorker extends AmazonMapReduceJobWorker{

    /**
     * This method returns properties related to the post processing job that the caller
     * might be interested in.  If there are no properties, an empty map should be returned.
     *
     * @return map 
     */
    public Map<String, String> getBubbleableProperties();

}
