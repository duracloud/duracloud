/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonfixity;

import org.duracloud.client.ContentStore;
import org.duracloud.storage.domain.HadoopTypes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS.JAR_CONTENT_ID;

/**
 * @author Andrew Woods
 *         Date: Oct 13, 2010
 */
public class AmazonFixityJobWorkerTest {

    private AmazonFixityJobWorker worker;

    @Before
    public void setUp() throws Exception {
        ContentStore contentStore = null;
        String workSpaceId = null;
        Map<String, String> taskParams = null;
        String serviceWorkDir = null;
        worker = new AmazonFixityJobWorker(contentStore,
                                           workSpaceId,
                                           taskParams,
                                           serviceWorkDir);
    }

    @Test
    public void testGetParamToResourceFileMap() throws Exception {
        Map<String, String> map = worker.getParamToResourceFileMap();
        Assert.assertNotNull(map);

        Assert.assertEquals(1, map.size());
        Assert.assertTrue(map.containsKey(JAR_CONTENT_ID.name()));
        Assert.assertEquals(map.get(JAR_CONTENT_ID.name()),
                            "fixity-processor.hjar");
    }
}
