/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonfixity;

import org.duracloud.services.ComputeService;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReduceService;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service runs the fixity service in the Amazon elastic map reduce framework.
 *
 * @author Andrew Woods
 *         Date: Sept 21, 2010
 */
public class AmazonFixityService extends BaseAmazonMapReduceService implements ManagedService, ComputeService {

    private final Logger log = LoggerFactory.getLogger(AmazonFixityService.class);

    private AmazonMapReduceJobWorker worker;
    private AmazonMapReduceJobWorker postWorker; // todo: add header to csv file and set mime

    @Override
    protected AmazonMapReduceJobWorker getJobWorker() {
        if (null == worker) {
            worker = new AmazonFixityJobWorker(getContentStore(),
                                         getWorkSpaceId(),
                                         collectTaskParams(),
                                         getServiceWorkDir());
        }
        return worker;
    }

    @Override
    protected AmazonMapReduceJobWorker getPostJobWorker() {
        return null;
    }

    @Override
    protected String getJobType() {
        return "amazon-fixity";
    }

    public void setWorker(AmazonMapReduceJobWorker worker) {
        this.worker = worker;
    }

    public void setPostWorker(AmazonMapReduceJobWorker postWorker) {
        this.postWorker = postWorker;
    }

}