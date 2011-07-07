/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce;

import org.duracloud.services.ComputeService;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker.JobStatus;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 5/26/11
 */
public class BaseAmazonMapReduceServiceGetPropsTest {

    private BaseAmazonMapReduceService service;

    private AmazonMapReduceJobWorker worker;
    private AmazonMapReduceJobWorker postWorker;

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(worker);
        if (null != postWorker) {
            EasyMock.verify(postWorker);
        }
    }

    @Test
    public void testGetServiceProps0() throws Exception {
        JobStatus jobStatus = JobStatus.STARTING;
        String jobId = null;
        String error = "something horrible";
        worker = createMockWorker(jobStatus, jobId, error);
        postWorker = null;
        service = new BaseAmazonMapReduceServiceImpl(worker, postWorker);

        verifyProps(ComputeService.ServiceStatus.PROCESSING, jobId, error);
    }

    @Test
    public void testGetServiceProps1() throws Exception {
        doTestGetServiceProps(JobStatus.STARTING);
        doTestGetServiceProps(JobStatus.RUNNING);
        doTestGetServiceProps(JobStatus.COMPLETE);
    }

    private void doTestGetServiceProps(JobStatus jobStatus) {
        String jobId = "job-id";
        String error = null;
        worker = createMockWorker(jobStatus, jobId, error);
        postWorker = null;
        service = new BaseAmazonMapReduceServiceImpl(worker, postWorker);

        verifyProps(jobStatus.toServiceStatus(), jobId, error);
    }

    @Test
    public void testGetServiceProps2() throws Exception {
        doTestGetServicePropsPost(JobStatus.WAITING);
        doTestGetServicePropsPost(JobStatus.POST_PROCESSING);
        doTestGetServicePropsPost(JobStatus.COMPLETE);
    }

    private void doTestGetServicePropsPost(JobStatus postJobStatus) {
        JobStatus jobStatus = JobStatus.COMPLETE;
        String jobId = "job-id";
        String error = null;
        worker = createMockWorker(jobStatus, jobId, error);

        String postJobId = "job-id2";
        String postError = null;
        postWorker = createMockWorker(postJobStatus, postJobId, postError);
        service = new BaseAmazonMapReduceServiceImpl(worker, postWorker);

        verifyProps(postJobStatus.toServiceStatus(), postJobId, error);
    }

    @Test
    public void testGetServicePropsPost3() {
        JobStatus jobStatus = JobStatus.COMPLETE;
        String jobId = "job-id";
        String error = "something bad";
        worker = createMockWorker(jobStatus, jobId, error);

        JobStatus postJobStatus = JobStatus.COMPLETE;
        String postJobId = "job-id2";
        String postError = "something worse";
        postWorker = createMockWorker(postJobStatus, postJobId, postError);
        service = new BaseAmazonMapReduceServiceImpl(worker, postWorker);

        verifyProps(ComputeService.ServiceStatus.FINALIZING,
                    postJobId,
                    postError);
    }

    private void verifyProps(ComputeService.ServiceStatus serviceStatus,
                             String jobId,
                             String error) {
        Map<String, String> props = service.getServiceProps();
        Assert.assertNotNull(props);

        // verify job status
        String statusProp = props.get(ComputeService.STATUS_KEY);
        Assert.assertNotNull(statusProp);

        ComputeService.ServiceStatus status = ComputeService.ServiceStatus
            .valueOf(statusProp);
        Assert.assertNotNull(status);
        Assert.assertEquals(serviceStatus, status);

        // verify job id
        String jobIdProp = props.get(ComputeService.SYSTEM_PREFIX + "Job ID");
        if (null == jobId) {
            Assert.assertNull(jobIdProp);
        } else {
            Assert.assertNotNull(jobIdProp);
            Assert.assertEquals(jobId, jobIdProp);
        }

        // verify error
        String errorProp = props.get(ComputeService.ERROR_KEY);
        if (null == error) {
            Assert.assertNull(errorProp);
        } else {
            Assert.assertNotNull(errorProp);
            Assert.assertEquals(error, errorProp);
        }
    }

    private AmazonMapReduceJobWorker createMockWorker(JobStatus status,
                                                      String jobId,
                                                      String error) {
        AmazonMapReduceJobWorker worker = EasyMock.createMock(
            "AmazonMapReduceJobWorker",
            AmazonMapReduceJobWorker.class);

        EasyMock.expect(worker.getJobStatus()).andReturn(status).times(2);
        EasyMock.expect(worker.getError()).andReturn(error);
        EasyMock.expect(worker.getJobId()).andReturn(jobId);
        if (null != jobId) {
            Map<String, String> map = new HashMap<String, String>();
            EasyMock.expect(worker.getJobDetailsMap()).andReturn(map);
        }

        EasyMock.replay(worker);
        return worker;
    }

    /**
     * This class is mock implementation of the BaseAmazonMapReduceService
     * abstract class.
     */
    private class BaseAmazonMapReduceServiceImpl extends BaseAmazonMapReduceService {
        private AmazonMapReduceJobWorker worker;
        private AmazonMapReduceJobWorker postWorker;

        public BaseAmazonMapReduceServiceImpl(AmazonMapReduceJobWorker worker,
                                              AmazonMapReduceJobWorker postWorker) {
            this.worker = worker;
            this.postWorker = postWorker;
        }

        protected AmazonMapReduceJobWorker getJobWorker() {
            return worker;
        }

        protected AmazonMapReduceJobWorker getPostJobWorker() {
            return postWorker;
        }

        protected String getJobType() {
            return null;
        }

        protected String getNumMappers(String instanceType) {
            return null;
        }
    }
}
