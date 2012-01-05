/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.impl;

import org.duracloud.services.BaseService;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.JobCompletionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Andrew Woods
 *         Date: 6/7/11
 */
public class SimpleJobCompletionMonitor extends JobCompletionMonitor {
    private final Logger log = LoggerFactory.getLogger(
        SimpleJobCompletionMonitor.class);

    private BaseService service;

    public SimpleJobCompletionMonitor(AmazonMapReduceJobWorker worker,
                                      BaseService service) {
        super(worker);
        this.service = service;
    }

    public SimpleJobCompletionMonitor(AmazonMapReduceJobWorker worker,
                                      BaseService service,
                                      long sleepMillis) {
        super(worker, sleepMillis);
        this.service = service;
    }

    @Override
    protected void preCompletionAction(AmazonMapReduceJobWorker worker) {
        log.debug("not implemented: preCompletionAction()");
    }

    @Override
    protected void postCompletionAction() {
        service.doneWorking();
    }

}
