/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.impl;

import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.JobCompletionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 6/7/11
 */
public class HadoopJobCompletionMonitor extends JobCompletionMonitor {
    private final Logger log = LoggerFactory.getLogger(
        HadoopJobCompletionMonitor.class);

    public HadoopJobCompletionMonitor(AmazonMapReduceJobWorker worker) {
        super(worker);
    }

    public HadoopJobCompletionMonitor(AmazonMapReduceJobWorker worker,
                                      long sleepMillis) {
        super(worker, sleepMillis);
    }

    @Override
    protected void preCompletionAction(AmazonMapReduceJobWorker worker) {
        // is hadoop job finished?
        Map<String, String> map = worker.getJobDetailsMap();
        for (String key : map.keySet()) {
            if (key.equals("Job State")) {
                String state = map.get(key);
                if ("COMPLETED".equals(state) || "FAILED".equals(state)) {
                    worker.shutdown();
                }
            }
        }
    }

    @Override
    protected void postCompletionAction() {
        log.debug("not implemented: postCompletionAction()");
    }
}
