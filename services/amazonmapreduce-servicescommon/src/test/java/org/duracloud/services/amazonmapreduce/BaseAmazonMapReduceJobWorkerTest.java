/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce;

import org.duracloud.client.ContentStore;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.domain.HadoopTypes;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker.JobStatus;
import static org.duracloud.storage.domain.HadoopTypes.RUN_HADOOP_TASK_NAME;
import static org.duracloud.storage.domain.HadoopTypes.TASK_OUTPUTS;
import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * @author Andrew Woods
 *         Date: Sep 30, 2010
 */
public class BaseAmazonMapReduceJobWorkerTest {

    private BaseAmazonMapReduceJobWorker worker;

    private ContentStore contentStore;
    private Map<String, String> taskParams;
    private String workSpaceId = "work-space-id";

    private File workDir = new File("target", "base-amr-job-worker-test");
    private String serviceWorkDir;

    private String jobId = "j-ABC";
    private String junkJar = "junk.jar";
    private String junkSh = "junk.sh";

    @Before
    public void setUp() throws Exception {
        if (!workDir.exists()) {
            Assert.assertTrue(workDir.mkdirs());
        }
        serviceWorkDir = workDir.getAbsolutePath();
        createFile(junkJar);
        createFile(junkSh);

        taskParams = createTaskParamsMap();
        contentStore = createMockContentStore();
        worker = createAmazonMapReduceJobWorker();

        // run it for each test
        worker.run();
    }

    private void createFile(String filename) throws IOException {
        FileOutputStream fileStream = new FileOutputStream(new File(workDir,
                                                                    filename));
        fileStream.write(new String("hello - " + filename).getBytes());
        fileStream.close();
    }

    private ContentStore createMockContentStore()
        throws ContentStoreException, FileNotFoundException {
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);

        Map<String, String> taskReturnMap = new HashMap<String, String>();
        taskReturnMap.put(TASK_OUTPUTS.JOB_FLOW_ID.name(), jobId);
        String taskReturn = SerializationUtil.serializeMap(taskReturnMap);

        EasyMock.expect(contentStore.performTask(EasyMock.eq(
            RUN_HADOOP_TASK_NAME), EasyMock.isA(String.class))).andReturn(
            taskReturn).times(1);

        EasyMock.expect(contentStore.performTask(EasyMock.eq(HadoopTypes.DESCRIBE_JOB_TASK_NAME),
                                                 EasyMock.isA(String.class)))
            .andReturn(taskReturn)
            .anyTimes();

        contentStore.createSpace(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(2);

        EasyMock.expect(contentStore.addContent(EasyMock.isA(String.class),
                                                EasyMock.isA(String.class),
                                                EasyMock.isA(InputStream.class),
                                                EasyMock.anyLong(),
                                                EasyMock.isA(String.class),
                                                EasyMock.<String>isNull(),
                                                EasyMock.<Map<String, String>>isNull()))
            .andReturn(null)
            .times(2);

        EasyMock.expect(contentStore.performTask(HadoopTypes.STOP_JOB_TASK_NAME,
                                                 jobId)).andReturn("stopped");

        EasyMock.makeThreadSafe(contentStore, true);
        EasyMock.replay(contentStore);

        return contentStore;
    }

    private BaseAmazonMapReduceJobWorker createAmazonMapReduceJobWorker() {
        return new BaseAmazonMapReduceJobWorker(contentStore,
                                                workSpaceId,
                                                taskParams,
                                                serviceWorkDir) {

            @Override
            protected Map<String, String> getParamToResourceFileMap() {
                return createResourceFileToParamMap();
            }
        };
    }

    private Map<String, String> createTaskParamsMap() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("source-space-id", TASK_PARAMS.SOURCE_SPACE_ID.name());
        params.put("dest-space-id", TASK_PARAMS.DEST_SPACE_ID.name());
        return params;
    }

    private Map<String, String> createResourceFileToParamMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(TASK_PARAMS.JAR_CONTENT_ID.name(), "junk.jar");
        map.put(TASK_PARAMS.BOOTSTRAP_CONTENT_ID.name(), "junk.sh");

        return map;
    }

    @After
    public void tearDown() throws Exception {
        if (!worker.getJobStatus().isComplete()) {
            worker.shutdown();
        }
        EasyMock.verify(contentStore);
    }

    @Test
    public void testRun() throws Exception {
        Map<String, String> resourcesMap = createResourceFileToParamMap();
        Assert.assertNotNull(resourcesMap);
        Assert.assertTrue(resourcesMap.size() > 0);

        Map<String, String> taskParamsMap = createTaskParamsMap();
        Assert.assertNotNull(taskParamsMap);
        Assert.assertTrue(taskParamsMap.size() > 0);

        Assert.assertEquals(resourcesMap.size() + taskParamsMap.size(),
                            taskParams.size());

        verifyTaskParams(resourcesMap);
        verifyTaskParams(taskParamsMap);
    }

    private void verifyTaskParams(Map<String, String> map) {
        for (String key : map.keySet()) {
            Assert.assertTrue(taskParams.containsKey(key));
            Assert.assertEquals(map.get(key), taskParams.get(key));
        }
    }

    @Test
    public void testGetJobStatus() throws Exception {
        AmazonMapReduceJobWorker.JobStatus status = worker.getJobStatus();
        Assert.assertNotNull(status);
        Assert.assertEquals(JobStatus.RUNNING, status);
    }

    @Test
    public void testGetJobId() throws Exception {
        String id = worker.getJobId();
        Assert.assertNotNull(id);
        Assert.assertEquals(jobId, id);
    }

    @Test
    public void testGetError() throws Exception {
        Assert.assertNull(worker.getError());
    }

    @Test
    public void testShutdown() throws Exception {
        AmazonMapReduceJobWorker.JobStatus status = worker.getJobStatus();
        Assert.assertNotNull(status);
        Assert.assertEquals(JobStatus.RUNNING, status);

        worker.shutdown();

        status = worker.getJobStatus();
        Assert.assertNotNull(status);
        Assert.assertEquals(JobStatus.COMPLETE, status);
    }

    @Test
    public void testGetJobDetailsMap() throws Exception {
        Map<String, String> details = worker.getJobDetailsMap();
        Assert.assertNotNull(details);

        Assert.assertEquals(1, details.size());
        Assert.assertTrue(details.containsKey(TASK_OUTPUTS.JOB_FLOW_ID.name()));
        Assert.assertEquals(jobId,
                            details.get(TASK_OUTPUTS.JOB_FLOW_ID.name()));
    }
}
