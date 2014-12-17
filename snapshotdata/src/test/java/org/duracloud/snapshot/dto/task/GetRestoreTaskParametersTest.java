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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Daniel Bernstein
 *         Date: 8/7/14
 */
public class GetRestoreTaskParametersTest {

    @Test
    public void testSerializeRestoreId() {
        String restoreId = "restore-id";

        GetRestoreTaskParameters taskParams = new GetRestoreTaskParameters();
        taskParams.setRestoreId(restoreId);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"restoreId\":\""+restoreId+"\""));
    }

    @Test
    public void testSerializeSnapshotId() {
        String snapshotId = "snapshot-id";

        GetRestoreTaskParameters taskParams = new GetRestoreTaskParameters();
        taskParams.setSnapshotId(snapshotId);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult,
                   containsString("\"snapshotId\":\""+snapshotId +"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized = "{\"snapshotId\" : \"snapshot-id\", \"restoreId\" : \"\"}";

        GetRestoreTaskParameters taskParams =
            GetRestoreTaskParameters.deserialize(taskParamsSerialized);
        assertEquals("snapshot-id", taskParams.getSnapshotId());

         // Verify that empty params throw
        try {
            GetRestoreTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }

        taskParamsSerialized = "{\"snapshotId\" : \"\", \"restoreId\" : \"101\"}";

        taskParams =
            GetRestoreTaskParameters.deserialize(taskParamsSerialized);
        assertEquals("101", taskParams.getRestoreId());

         // Verify that empty params throw
        try {
            GetRestoreTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }

    }

    @Test
    public void testSnapshotIdAndRestoreIdMayNotBeNonNullAtTheSameTime(){
        // Verify that empty params throw
        try {
            GetRestoreTaskParameters.deserialize("{\"snapshotId\" : \"snapshot-id\", \"restoreId\" : \"101\"}");
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }
        
    }
    
    @Test
    public void testSnapshotIdAndRestoreIdMayNotBeNullAtTheSameTime(){
        // Verify that empty params throw
        try {
            GetRestoreTaskParameters.deserialize("{\"snapshotId\" : \"\", \"restoreId\" : \"\"}");
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }
        
    }

}
