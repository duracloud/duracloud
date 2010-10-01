/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.hadoop;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.duracloud.storage.domain.HadoopTypes.HJAR_PARAMS;
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
        String dcHost = "dc-host";
        taskParams.put(TASK_PARAMS.DC_HOST.name(), dcHost);
        String dcPort = "dc-port";
        taskParams.put(TASK_PARAMS.DC_PORT.name(), dcPort);
        String dcContext = "dc-context";
        taskParams.put(TASK_PARAMS.DC_CONTEXT.name(), dcContext);
        String dcUsername = "dc-username";
        taskParams.put(TASK_PARAMS.DC_USERNAME.name(), dcUsername);
        String dcPassword = "dc-password";
        taskParams.put(TASK_PARAMS.DC_PASSWORD.name(), dcPassword);

        jarParams =
            helper.completeJarParams(taskParams, new ArrayList<String>());
        assertEquals(16, jarParams.size());
                
        assertEquals(HJAR_PARAMS.SOURCE_SPACE_ID.getParam(), jarParams.get(0));
        assertEquals(sourceSpaceId, jarParams.get(1));
        assertEquals(HJAR_PARAMS.REP_STORE_ID.getParam(), jarParams.get(2));
        assertEquals(repStoreId, jarParams.get(3));
        assertEquals(HJAR_PARAMS.REP_SPACE_ID.getParam(), jarParams.get(4));
        assertEquals(repSpaceId, jarParams.get(5));
        assertEquals(HJAR_PARAMS.DC_HOST.getParam(), jarParams.get(6));
        assertEquals(dcHost, jarParams.get(7));
        assertEquals(HJAR_PARAMS.DC_PORT.getParam(), jarParams.get(8));
        assertEquals(dcPort, jarParams.get(9));
        assertEquals(HJAR_PARAMS.DC_CONTEXT.getParam(), jarParams.get(10));
        assertEquals(dcContext, jarParams.get(11));
        assertEquals(HJAR_PARAMS.DC_USERNAME.getParam(), jarParams.get(12));
        assertEquals(dcUsername, jarParams.get(13));
        assertEquals(HJAR_PARAMS.DC_PASSWORD.getParam(), jarParams.get(14));
        assertEquals(dcPassword, jarParams.get(15));
    }

}
