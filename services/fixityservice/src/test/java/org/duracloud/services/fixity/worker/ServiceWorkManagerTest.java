/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.worker;

import org.duracloud.services.fixity.results.ServiceResultListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @author Andrew Woods
 *         Date: Aug 9, 2010
 */
public class ServiceWorkManagerTest {

    private ServiceWorkManager manager;

    private ServiceWorkload workload;
    private ServiceWorkerFactory workerFactory;
    private ServiceResultListener resultListener;
    private int threads = 3;
    private CountDownLatch doneWorking;

    @Before
    public void setUp() throws Exception {
        manager = new ServiceWorkManager(workload,
                                         workerFactory,
                                         resultListener,
                                         threads,
                                         doneWorking);

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRun() throws Exception {
    }

    @Test
    public void testGetProcessingStatus() throws Exception {
    }

    @Test
    public void testStopProcessing() throws Exception {
    }
}
