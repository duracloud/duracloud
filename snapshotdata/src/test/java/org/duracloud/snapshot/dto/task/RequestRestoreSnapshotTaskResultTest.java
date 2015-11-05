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
 *         Date: 11/04/15
 */
public class RequestRestoreSnapshotTaskResultTest {

    private String resultValue = "success";

    @Test
    public void testSerialize() {
        RequestRestoreSnapshotTaskResult result = new RequestRestoreSnapshotTaskResult();
        result.setDescription(resultValue);
        String description = result.serialize();
        String cleanResult = description.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"description\":\""+ String.valueOf(resultValue)+"\""));

    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized = "{\"description\" : \""+String.valueOf(resultValue)+"\"}";

        RequestRestoreSnapshotTaskResult taskResult =
            RequestRestoreSnapshotTaskResult.deserialize(taskParamsSerialized);
        assertEquals(resultValue, taskResult.getDescription());
    }

}
