/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Daniel Bernstein
 *         Date: 9/22/15
 */
public class CompleteCancelSnapshotTaskResultTest {

    private String resultValue = "success";

    @Test
    public void testSerialize() {
        String result = new CompleteCancelSnapshotTaskResult(resultValue).serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"result\":\""+ String.valueOf(resultValue)+"\""));

    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized = "{\"result\" : \""+String.valueOf(resultValue)+"\"}";

        CompleteCancelSnapshotTaskResult taskResult =
            CompleteCancelSnapshotTaskResult.deserialize(taskParamsSerialized);
        assertEquals(resultValue, taskResult.getResult());
    }

}
