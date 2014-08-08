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
import static org.junit.Assert.fail;

/**
 * @author Daniel Bernstein
 *         Date: 8/7/14
 */
public class GetRestoreTaskParametersTest {

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
        assertEquals(101, taskParams.getRestoreId().longValue());

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
