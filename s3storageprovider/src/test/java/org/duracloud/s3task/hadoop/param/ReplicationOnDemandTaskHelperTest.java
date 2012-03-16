/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.hadoop.param;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * @author: Bill Branan
 * Date: Sep 30, 2010
 */
public class ReplicationOnDemandTaskHelperTest {

    @Test
    public void testCompleteJarParams() {
        ReplicationOnDemandTaskHelper helper =
            new ReplicationOnDemandTaskHelper();

        Map<String, String> taskParams = new HashMap<String, String>();
        List<String> jarParams = new ArrayList<String>();

        try {
            helper.completeJarParams(taskParams, jarParams);
            fail("Exception expected when no task params provided");
        } catch(RuntimeException expected) {
            assertNotNull(expected);
        }

        String sourceSpaceId = "sourceSpaceId";
        taskParams.put(TASK_PARAMS.SOURCE_SPACE_ID.name(), sourceSpaceId);
        String repStoreId = "rep-store-id";
        taskParams.put(TASK_PARAMS.REP_STORE_ID.name(), repStoreId);
        String repSpaceId = "rep-space-id";
        taskParams.put(TASK_PARAMS.REP_SPACE_ID.name(), repSpaceId);

        jarParams =
            helper.completeJarParams(taskParams, new ArrayList<String>());
        assertEquals(6, jarParams.size());
                
        assertEquals(TASK_PARAMS.SOURCE_SPACE_ID.getCliForm(), jarParams.get(0));
        assertEquals(sourceSpaceId, jarParams.get(1));
        assertEquals(TASK_PARAMS.REP_STORE_ID.getCliForm(), jarParams.get(2));
        assertEquals(repStoreId, jarParams.get(3));
        assertEquals(TASK_PARAMS.REP_SPACE_ID.getCliForm(), jarParams.get(4));
        assertEquals(repSpaceId, jarParams.get(5));
    }

}
