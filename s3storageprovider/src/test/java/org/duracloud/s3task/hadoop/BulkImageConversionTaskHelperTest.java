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

        taskParams.put("destFormat", "png");
        jarParams =
            helper.completeJarParams(taskParams, new ArrayList<String>());
        assertEquals(2, jarParams.size());
        assertEquals("-f", jarParams.get(0));
        assertEquals("png", jarParams.get(1));

        taskParams.put("namePrefix", "test-");
        jarParams =
            helper.completeJarParams(taskParams, new ArrayList<String>());
        assertEquals(4, jarParams.size());
        assertEquals("-p", jarParams.get(2));
        assertEquals("test-", jarParams.get(3));

        taskParams.put("nameSuffix", "-test");
        jarParams =
            helper.completeJarParams(taskParams, new ArrayList<String>());
        assertEquals(6, jarParams.size());
        assertEquals("-s", jarParams.get(4));
        assertEquals("-test", jarParams.get(5));

        taskParams.put("colorSpace", "sRGB");
        jarParams =
            helper.completeJarParams(taskParams, new ArrayList<String>());
        assertEquals(8, jarParams.size());
        assertEquals("-c", jarParams.get(6));
        assertEquals("sRGB", jarParams.get(7));        
    }
}
