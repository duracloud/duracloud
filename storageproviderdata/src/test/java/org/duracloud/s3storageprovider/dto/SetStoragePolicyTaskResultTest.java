/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storageprovider.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 09/25/2015
 */
public class SetStoragePolicyTaskResultTest {

    private final String resultValue = "task-result";

    @Test
    public void testSerialize() {
        SetStoragePolicyTaskResult taskResult = new SetStoragePolicyTaskResult();
        taskResult.setResult(resultValue);

        String result = taskResult.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"result\":\""+resultValue+"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String resultSerialized = "{\"result\" : \""+resultValue+"\"}";

        SetStoragePolicyTaskResult taskResult =
            SetStoragePolicyTaskResult.deserialize(resultSerialized);
        assertEquals(resultValue, taskResult.getResult());
    }


}
