/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.services.amazonfixity.postprocessing;

import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReducePostJobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrew Woods
 *         Date: 2/9/11
 */
public class WrapperPostJobWorker extends BaseAmazonMapReducePostJobWorker {

    private final Logger log = LoggerFactory.getLogger(WrapperPostJobWorker.class);

    private AmazonMapReduceJobWorker targetWorker;

    public WrapperPostJobWorker(AmazonMapReduceJobWorker predecessor,
                                AmazonMapReduceJobWorker targetWorker) {
        super(predecessor);
        this.targetWorker = targetWorker;
    }

    public WrapperPostJobWorker(AmazonMapReduceJobWorker predecessor,
                                AmazonMapReduceJobWorker targetWorker,
                                long sleepMillis) {
        super(predecessor, sleepMillis);
        this.targetWorker = targetWorker;
    }

    @Override
    protected void doWork() {
        log.info("Start wrapped worker of class: " + targetWorker.getClass());

        // let previous hadoop servers clean up. (10-min)
        sleep(600000);

        targetWorker.run();
        while (!JobStatus.COMPLETE.equals(targetWorker.getJobStatus())) {
            sleep(sleepMillis);
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        targetWorker.shutdown();
    }
}
