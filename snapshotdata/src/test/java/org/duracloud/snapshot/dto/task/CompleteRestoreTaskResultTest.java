/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Test;

/**
 * @author Bill Branan
 * Date: 7/29/15
 */
public class CompleteRestoreTaskResultTest {

    private String resultValue = "result";

    @Test
    public void testSerialize() {

        String result = new CompleteRestoreTaskResult(resultValue).serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"result\":\"" + resultValue + "\""));

    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized = "{\"result\" : \"" + resultValue + "\"}";

        CompleteRestoreTaskResult taskResult =
            CompleteRestoreTaskResult.deserialize(taskParamsSerialized);
        assertEquals(resultValue, taskResult.getResult());
    }

}
