/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.worker;

import org.duracloud.services.fixity.results.NoopResultListener;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.results.ServiceResultProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * @author Andrew Woods
 *         Date: Aug 5, 2010
 */
public class PatientServiceWorkManager extends ServiceWorkManager {
    private final Logger log = LoggerFactory.getLogger(PatientServiceWorkManager.class);

    public PatientServiceWorkManager(ServiceWorkload workload,
                                     ServiceWorkerFactory workerFactory,
                                     ServiceResultListener resultListener,
                                     int threads,
                                     CountDownLatch doneWorking) {
        super(workload,
              workerFactory,
              resultListener,
              threads,
              doneWorking);
    }

    @Override
    public void run() {
        try {
            super.doneWorking.await();
            super.run();

        } catch (InterruptedException e) {
            log.warn("Error calling doneWorking.await(): " + e.getMessage(), e);
        }
    }

}
