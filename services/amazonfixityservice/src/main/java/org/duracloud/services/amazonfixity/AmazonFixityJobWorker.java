/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonfixity;

import org.duracloud.client.ContentStore;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReduceJobWorker;

import java.util.Map;

/**
 * @author: Andrew Woods
 * Date: Sept 21, 2010
 */
public class AmazonFixityJobWorker extends BaseAmazonMapReduceJobWorker {

    public AmazonFixityJobWorker(ContentStore contentStore,
                           String workSpaceId,
                           Map<String, String> taskParams,
                           String serviceWorkDir) {
        super(contentStore, workSpaceId, taskParams, serviceWorkDir);
    }

    @Override
    protected String getHadoopJarPrefix() {
        return "fixity-processor";
    }

}
