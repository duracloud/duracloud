/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto;

import org.duracloud.snapshot.error.SnapshotDataException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Bill Branan
 *         Date: 7/25/14
 */
public class CreateSnapshotTaskParametersTest {

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized =
            "{\"spaceId\" : \"test-space\"," +
            " \"description\" : \"test snapshot\"," +
            " \"userEmail\" : \"yo@myemail.com\"}";

        CreateSnapshotTaskParameters taskParams =
            CreateSnapshotTaskParameters.deserialize(taskParamsSerialized);
        assertEquals("test-space", taskParams.getSpaceId());
        assertEquals("test snapshot", taskParams.getDescription());
        assertEquals("yo@myemail.com", taskParams.getUserEmail());

        // Verify that empty params throw
        taskParamsSerialized =
            "{\"spaceId\" : \"\"," +
            " \"description\" : \"\"," +
            " \"userEmail\" : \"\"}";

        try {
            CreateSnapshotTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }

        // Verify that empty params throw
        try {
            CreateSnapshotTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }
    }

}
