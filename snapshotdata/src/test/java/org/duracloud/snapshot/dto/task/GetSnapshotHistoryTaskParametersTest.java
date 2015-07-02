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
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.duracloud.snapshot.error.SnapshotDataException;
import org.junit.Test;

/**
 * @author Gad Krumholz
 *         Date: 7/02/15
 */
public class GetSnapshotHistoryTaskParametersTest {

    private String snapshotId = "snapshot-id";
    private int pageNumber = 22;
    private int pageSize = 44;

    @Test
    public void testSerialize() {
        GetSnapshotHistoryTaskParameters taskParams =
            new GetSnapshotHistoryTaskParameters();
        taskParams.setSnapshotId(snapshotId);
        taskParams.setPageNumber(pageNumber);
        taskParams.setPageSize(pageSize);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"snapshotId\":\""+ snapshotId +"\""));
        assertThat(cleanResult, containsString("\"pageNumber\":"+ pageNumber +""));
        assertThat(cleanResult, containsString("\"pageSize\":"+ pageSize +""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized =
            "{\"snapshotId\" : \""+snapshotId+"\"," +
            "\"pageNumber\" : "+pageNumber+"," +
            " \"pageSize\" : "+pageSize+"}";

        GetSnapshotHistoryTaskParameters taskParams =
                GetSnapshotHistoryTaskParameters.deserialize(taskParamsSerialized);
        assertEquals(snapshotId, taskParams.getSnapshotId());
        assertEquals(pageNumber, taskParams.getPageNumber());
        assertEquals(pageSize, taskParams.getPageSize());

        // Verify that empty params throw
        taskParamsSerialized = "{\"snapshotId\" : \"\"}";

        try {
            GetSnapshotHistoryTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }

        // Verify that empty params throw
        try {
            GetSnapshotHistoryTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }
    }

}
