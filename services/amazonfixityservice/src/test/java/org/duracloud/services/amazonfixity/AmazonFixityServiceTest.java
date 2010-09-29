/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonfixity;

import org.duracloud.client.ContentStore;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.domain.HadoopTypes;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.RUN_HADOOP_TASK_NAME;
import static org.duracloud.storage.domain.HadoopTypes.TASK_OUTPUTS;

/**
 * @author Andrew Woods
 *         Date: Sep 28, 2010
 */
public class AmazonFixityServiceTest {

    private AmazonFixityService service;

    @Before
    public void setUp() throws Exception {
        service = new AmazonFixityService();
        service.setContentStore(createMockContentStore());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testStart() throws Exception {
        service.start();
    }

    @Test
    public void testStop() throws Exception {
    }

    @Test
    public void testGetServiceProps() throws Exception {
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
            .andReturn(null)
            .times(2);

        EasyMock.replay(contentStore);
        return contentStore;
    }
}
