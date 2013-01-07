/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.bulkimageconversion;

import org.apache.commons.io.FileUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.error.ContentStoreException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker.JobStatus;
import static org.duracloud.storage.domain.HadoopTypes.RUN_HADOOP_TASK_NAME;
import static org.duracloud.storage.domain.HadoopTypes.TASK_OUTPUTS;

/**
 * @author: Bill Branan
 * Date: Aug 23, 2010
 */
public class HadoopJobWorkerTest {

    private ContentStore contentStore;
    private File workDir;

    @Before
    public void setUp() throws Exception {
        workDir = new File("target/work");
        workDir.mkdir();
        File processor = new File(workDir, "image-conversion-processor.hjar");
        FileUtils.writeStringToFile(processor, "processor");
        File bootScript = new File(workDir, "install-image-magick.sh");
        FileUtils.writeStringToFile(bootScript, "bootscript");

        contentStore = createMockContentStore();
    }

    private ContentStore createMockContentStore()
        throws ContentStoreException {
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);

        Map<String, String> taskReturnMap = new HashMap<String, String>();
        taskReturnMap.put(TASK_OUTPUTS.JOB_FLOW_ID.name(), "1");
        String taskReturn = SerializationUtil.serializeMap(taskReturnMap);

        EasyMock
            .expect(contentStore.performTask(EasyMock.eq(RUN_HADOOP_TASK_NAME),
                                             EasyMock.isA(String.class)))
            .andReturn(taskReturn)
            .times(1);

        contentStore.createSpace(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(2);

        EasyMock
            .expect(contentStore.addContent(EasyMock.isA(String.class),
                                            EasyMock.isA(String.class),
                                            EasyMock.isA(InputStream.class),
                                            EasyMock.anyLong(),
                                            EasyMock.isA(String.class),
                                            EasyMock.<String>isNull(),
                                            EasyMock.<Map<String, String>>isNull()))
            .andReturn(null)
            .times(2);

        EasyMock.replay(contentStore);
        return contentStore;
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
        contentStore = null;

        FileUtils.deleteQuietly(workDir);
    }

    @Test
    public void TestWorker() {
        String workSpaceId = "work-space";
        Map<String, String> taskParams = new HashMap<String, String>();
        String serviceWorkDir = workDir.getAbsolutePath();

        HadoopJobWorker worker = new HadoopJobWorker(contentStore,
                                                     workSpaceId,
                                                     taskParams,
                                                     serviceWorkDir);

        worker.run();
        assertEquals(JobStatus.RUNNING, worker.getJobStatus());
        assertNull(worker.getError());
        assertEquals("1", worker.getJobId());
    }

}
