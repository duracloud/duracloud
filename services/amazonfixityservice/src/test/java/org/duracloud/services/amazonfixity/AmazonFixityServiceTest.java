/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonfixity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.ComputeService;
import org.duracloud.storage.domain.HadoopTypes;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.duracloud.storage.domain.HadoopTypes.DESCRIBE_JOB_TASK_NAME;
import static org.duracloud.storage.domain.HadoopTypes.RUN_HADOOP_TASK_NAME;
import static org.duracloud.storage.domain.HadoopTypes.TASK_OUTPUTS;
import static org.duracloud.storage.domain.HadoopTypes.JOB_TYPES;

/**
 * @author Andrew Woods
 *         Date: Sep 28, 2010
 */
public class AmazonFixityServiceTest {

    private AmazonFixityService service;
    private ContentStore contentStore;

    private File serviceWorkDir = new File("target", "test-fixity-service");

    @Before
    public void setUp() {
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);
        service = new AmazonFixityService();
    }

    private void verifyMocks() {
        EasyMock.verify(contentStore);
    }

    @Test
    public void testGetJobType() {
        String jobType = service.getJobType();
        Assert.assertNotNull(jobType);
        Assert.assertEquals(JOB_TYPES.AMAZON_FIXITY.name(), jobType);
    }

    @Test
    public void testGetNumMappers() {
        String num = service.getNumMappers(HadoopTypes.INSTANCES.SMALL.getId());
        verifyNumMappers(num, "2");

        num = service.getNumMappers(HadoopTypes.INSTANCES.LARGE.getId());
        verifyNumMappers(num, "4");

        num = service.getNumMappers(HadoopTypes.INSTANCES.XLARGE.getId());
        verifyNumMappers(num, "8");
    }

    private void verifyNumMappers(String num, String expected) {
        Assert.assertNotNull(num);
        Assert.assertEquals(expected, num);
    }

    @Test
    public void testOptmizationConfig() {
        String instanceType = "m1.xlarge";
        String numOfInstances = "10";

        AmazonFixityService service = new AmazonFixityService();
        service.setOptimizeMode("standard");
        service.setOptimizeType("optimize_for_speed");
        service.setSpeedInstanceType(instanceType);
        service.setSpeedNumInstances(numOfInstances);
        assertEquals(instanceType,service.getInstancesType());
        assertEquals(numOfInstances,service.getNumOfInstances());

        instanceType = "m1.large";
        numOfInstances = "3";
        service.setOptimizeType("optimize_for_cost");
        service.setSpeedInstanceType(null);
        service.setSpeedNumInstances(null);
        service.setCostInstanceType(instanceType);
        service.setCostNumInstances(numOfInstances);
        assertEquals(instanceType,service.getInstancesType());
        assertEquals(numOfInstances,service.getNumOfInstances());
    }

    @Test
    public void testStart() throws Exception {
        setUpStart();

        service.start();
        ComputeService.ServiceStatus status = service.getServiceStatus();
        Assert.assertNotNull(status);
        Assert.assertEquals(ComputeService.ServiceStatus.STARTING, status);

        sleep(2000); // do some work

        Map<String, String> props = service.getServiceProps();
        Assert.assertNotNull(props);
        Assert.assertTrue(props.containsKey(ComputeService.STARTTIME_KEY));
        Assert.assertFalse(props.containsKey(ComputeService.STOPTIME_KEY));

        verifyMocks();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing.
        }
    }

    private void setUpStart() throws Exception {
        setStartMockExpectationsFor(contentStore);
        EasyMock.replay(contentStore);

        setUpService();
    }

    private void setUpService() throws IOException {
        service.setServiceId("test-fixityservice");
        service.setWorkSpaceId("work-space-id");
        service.setOptimizeMode("standard");
        service.setOptimizeType("optimize_for_cost");
        service.setMode("all-in-one-for-list");
        service.setContentStore(contentStore);

        String workDir = serviceWorkDir.getAbsolutePath();
        if (!serviceWorkDir.exists()) {
            Assert.assertTrue(workDir, serviceWorkDir.mkdir());
        }
        service.setServiceWorkDir(workDir);

        OutputStream hjar = FileUtils.openOutputStream(new File(workDir,
                                                                "fixity-processor.hjar"));
        IOUtils.write("hello", hjar);
        IOUtils.closeQuietly(hjar);
    }

    private void setStartMockExpectationsFor(ContentStore contentStore) throws ContentStoreException {
        Map<String, String> taskReturnMap = new HashMap<String, String>();
        taskReturnMap.put(TASK_OUTPUTS.JOB_FLOW_ID.name(), "1");
        String taskReturn = SerializationUtil.serializeMap(taskReturnMap);

        EasyMock.expect(contentStore.performTask(EasyMock.eq(
            RUN_HADOOP_TASK_NAME), EasyMock.isA(String.class))).andReturn(
            taskReturn).times(1);

        contentStore.createSpace(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(1);

        EasyMock.expect(contentStore.getStoreId()).andReturn("0");
        EasyMock.expect(contentStore.addContent(EasyMock.isA(String.class),
                                                EasyMock.isA(String.class),
                                                EasyMock.isA(InputStream.class),
                                                EasyMock.anyLong(),
                                                EasyMock.isA(String.class),
                                                EasyMock.<String>isNull(),
                                                EasyMock.<Map<String, String>>isNull()))
            .andReturn(null);

        EasyMock.expect(contentStore.performTask(EasyMock.isA(String.class),
                                                 EasyMock.isA(String.class)))
            .andReturn(null);
        EasyMock.expect(contentStore.performTask(EasyMock.eq(DESCRIBE_JOB_TASK_NAME),
                                                 EasyMock.isA(String.class)))
            .andReturn(null);

        EasyMock.makeThreadSafe(contentStore, true);
    }

    //FIXME For some reason which I do not yet understand, this test is failing, but only when building cleanly with maven.
    //mvn test -Dtest=AmazonFixityServiceTest runs successfully. 
    //running the test in my IDE also succeeds.  --Danny
    //@Test
    public void testShutdown() throws Exception {
        setUpShutdown();

        service.start();
        ComputeService.ServiceStatus status = service.getServiceStatus();
        Assert.assertNotNull(status);
        Assert.assertEquals(ComputeService.ServiceStatus.STARTING, status);
        
        Map<String,String> props = service.getServiceProps();

        sleep(500); // do some work

        /*
         This is effectively a manual test.
         The test "passes" if the output contains log messages for each of the
         wrapped JobWorker and PostJobWorkers:
            Stopping test-fixityservice, org.duracloud.services.amazonfixity.AmazonFixityService
            shutting down: org.duracloud.services.amazonfixity.AmazonFixityJobWorker
            shutting down: org.duracloud.services.amazonmapreduce.postprocessing.MultiPostJobWorker
            shutting down: org.duracloud.services.amazonmapreduce.postprocessing.HeaderPostJobWorker
            shutting down: org.duracloud.services.amazonfixity.postprocessing.WrapperPostJobWorker
            shutting down: org.duracloud.services.amazonfixity.AmazonFixityPropertiesJobWorker
            shutting down: org.duracloud.services.amazonmapreduce.postprocessing.HeaderPostJobWorker
            shutting down: org.duracloud.services.amazonfixity.postprocessing.VerifyHashesPostJobWorker
            FixityService is Stopping
            shutting down: org.duracloud.services.amazonmapreduce.postprocessing.MimePostJobWorker
            shutting down: org.duracloud.services.amazonmapreduce.postprocessing.DeletePostJobWorker
         */
        service.stop();

        props = service.getServiceProps();
        Assert.assertNotNull(props);
        Assert.assertTrue(props.containsKey(ComputeService.STARTTIME_KEY));
        Assert.assertTrue(props.containsKey(ComputeService.STOPTIME_KEY));
        verifyMocks();
    }

    private void setUpShutdown() throws Exception {
        setStartMockExpectationsFor(contentStore);
        setStopMockExpectationsfor(contentStore);

        EasyMock.replay(contentStore);

        setUpService();
    }

    private void setStopMockExpectationsfor(ContentStore contentStore)
        throws ContentStoreException {
        Map<String, String> taskReturnMap = new HashMap<String, String>();
        taskReturnMap.put(TASK_OUTPUTS.JOB_FLOW_ID.name(), "1");
        String taskReturn = SerializationUtil.serializeMap(taskReturnMap);

        EasyMock.expect(contentStore.performTask(EasyMock.eq(
            HadoopTypes.DESCRIBE_JOB_TASK_NAME), EasyMock.<String>isNull())).andReturn(
            taskReturn).times(1);

        EasyMock.expect(contentStore.performTask(EasyMock.eq(
            HadoopTypes.STOP_JOB_TASK_NAME), EasyMock.isA(String.class))).andReturn(
            taskReturn).times(1);

        EasyMock.expect(contentStore.getStoreId()).andReturn("0");
    }

}
