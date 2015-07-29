/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.task;

import org.duracloud.snapshot.error.SnapshotDataException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 7/29/15
 */
public class CompleteRestoreTaskParametersTest {

    @Test
    public void testSerialize() {
        String spaceId = "space-id";
        int daysToExpire = 10;

        CompleteRestoreTaskParameters taskParams =
            new CompleteRestoreTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setDaysToExpire(daysToExpire);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"spaceId\":\""+ spaceId +"\""));
        assertThat(cleanResult, containsString("\"daysToExpire\":"+ daysToExpire +""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized = "{\"spaceId\" : \"test-space\", " +
                                       "\"daysToExpire\" : 20}";

        CompleteRestoreTaskParameters taskParams =
            CompleteRestoreTaskParameters.deserialize(taskParamsSerialized);
        assertEquals("test-space", taskParams.getSpaceId());
        assertEquals(20, taskParams.getDaysToExpire());

        // Verify that invalid parmas throw
        taskParamsSerialized = "{\"spaceId\" : \"test-space\", " +
                               "\"daysToExpire\" : -1}";

        try {
            CompleteRestoreTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }

        // Verify that empty params throw
        taskParamsSerialized = "{\"spaceId\" : \"\"}";

        try {
            CompleteRestoreTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }

        // Verify that empty params throw
        try {
            CompleteRestoreTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }
    }

}
