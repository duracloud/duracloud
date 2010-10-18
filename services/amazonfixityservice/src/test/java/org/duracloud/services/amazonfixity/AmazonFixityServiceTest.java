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
import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

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
        service = new AmazonFixityService();
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
    public void testStart() throws Exception {
        setUpStart();

        service.start();
        ComputeService.ServiceStatus status = service.getServiceStatus();
        Assert.assertNotNull(status);
        Assert.assertEquals(ComputeService.ServiceStatus.STARTED, status);

        sleep(1000); // do some work

        verifyStart();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing.
        }
    }

    private void setUpStart() throws Exception {
        service.setWorkSpaceId("work-space-id");

        contentStore = createMockContentStore();
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

    private ContentStore createMockContentStore() throws ContentStoreException {
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);

        Map<String, String> taskReturnMap = new HashMap<String, String>();
        taskReturnMap.put(TASK_OUTPUTS.JOB_FLOW_ID.name(), "1");
        String taskReturn = SerializationUtil.serializeMap(taskReturnMap);

        EasyMock.expect(contentStore.performTask(EasyMock.eq(
            RUN_HADOOP_TASK_NAME), EasyMock.isA(String.class))).andReturn(
            taskReturn).times(1);

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

        EasyMock.makeThreadSafe(contentStore, true);
        EasyMock.replay(contentStore);
        return contentStore;
    }

    private void verifyStart() {
        EasyMock.verify(contentStore);
    }

}
