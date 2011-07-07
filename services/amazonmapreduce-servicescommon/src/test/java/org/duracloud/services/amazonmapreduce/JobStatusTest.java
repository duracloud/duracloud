/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.duracloud.services.ComputeService.ServiceStatus;
import static org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker.JobStatus;


/**
 * @author Andrew Woods
 *         Date: 5/26/11
 */
public class JobStatusTest {

    private final int NUM_STATES = 6;

    @Before
    public void setUp() {
        Assert.assertEquals(NUM_STATES, JobStatus.values().length);
    }

    @Test
    public void testToServiceStatus() {
        JobStatus status = JobStatus.STARTING;
        Assert.assertEquals(ServiceStatus.PROCESSING, status.toServiceStatus());

        status = JobStatus.RUNNING;
        Assert.assertEquals(ServiceStatus.PROCESSING, status.toServiceStatus());

        status = JobStatus.COMPLETE;
        Assert.assertEquals(ServiceStatus.FINALIZING, status.toServiceStatus());

        status = JobStatus.WAITING;
        Assert.assertEquals(ServiceStatus.WAITING, status.toServiceStatus());

        status = JobStatus.POST_PROCESSING;
        Assert.assertEquals(ServiceStatus.POSTPROCESSING,
                            status.toServiceStatus());

        status = JobStatus.UNKNOWN;
        Assert.assertNull(status.toServiceStatus());
    }
}
