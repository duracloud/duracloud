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
 * Date: Aug 23, 2010
 */
public class BulkImageConversionTaskHelperTest {

    @Test
    public void testCompleteJarParams() {
        BulkImageConversionTaskHelper helper =
            new BulkImageConversionTaskHelper();

        Map<String, String> taskParams = new HashMap<String, String>();
        List<String> jarParams = new ArrayList<String>();

        try {
            helper.completeJarParams(taskParams, jarParams);
            fail("Exception expected when no task params provided");
        } catch(RuntimeException expected) {
            assertNotNull(expected);
        }

        taskParams.put(TASK_PARAMS.DEST_FORMAT.name(), "png");
        jarParams =
            helper.completeJarParams(taskParams, new ArrayList<String>());
        assertEquals(2, jarParams.size());
        assertEquals(HJAR_PARAMS.DEST_FORMAT.getParam(), jarParams.get(0));
        assertEquals("png", jarParams.get(1));

        taskParams.put(TASK_PARAMS.NAME_PREFIX.name(), "test-");
        jarParams =
            helper.completeJarParams(taskParams, new ArrayList<String>());
        assertEquals(4, jarParams.size());
        assertEquals(HJAR_PARAMS.NAME_PREFIX.getParam(), jarParams.get(2));
        assertEquals("test-", jarParams.get(3));

        taskParams.put(TASK_PARAMS.NAME_SUFFIX.name(), "-test");
        jarParams =
            helper.completeJarParams(taskParams, new ArrayList<String>());
        assertEquals(6, jarParams.size());
        assertEquals(HJAR_PARAMS.NAME_SUFFIX.getParam(), jarParams.get(4));
        assertEquals("-test", jarParams.get(5));

        taskParams.put(TASK_PARAMS.COLOR_SPACE.name(), "sRGB");
        jarParams =
            helper.completeJarParams(taskParams, new ArrayList<String>());
        assertEquals(8, jarParams.size());
        assertEquals(HJAR_PARAMS.COLOR_SPACE.getParam(), jarParams.get(6));
        assertEquals("sRGB", jarParams.get(7));        
    }
}
