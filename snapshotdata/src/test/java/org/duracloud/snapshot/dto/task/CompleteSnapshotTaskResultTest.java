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
 * @author Bill Branan
 *         Date: 7/25/14
 */
public class CompleteSnapshotTaskResultTest {

    private int expirationDays = 42;

    @Test
    public void testSerialize() {

        String result = new CompleteSnapshotTaskResult(expirationDays).serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult,
                   containsString("\"contentExpirationDays\":"+expirationDays));

    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized =
            "{\"contentExpirationDays\" : "+expirationDays+"}";

        CompleteSnapshotTaskResult taskResult =
            CompleteSnapshotTaskResult.deserialize(taskParamsSerialized);
        assertEquals(expirationDays, taskResult.getContentExpirationDays());
    }

}
