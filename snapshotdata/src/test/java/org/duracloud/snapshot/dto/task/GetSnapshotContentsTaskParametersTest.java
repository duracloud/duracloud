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
 * @author Bill Branan
 *         Date: 8/11/14
 */
public class GetSnapshotContentsTaskParametersTest {

    private String snapshotId = "snapshot-id";
    private int pageNumber = 22;
    private int pageSize = 44;
    private String prefix = "prefix";

    @Test
    public void testSerialize() {
        GetSnapshotContentsTaskParameters taskParams =
            new GetSnapshotContentsTaskParameters();
        taskParams.setSnapshotId(snapshotId);
        taskParams.setPageNumber(pageNumber);
        taskParams.setPageSize(pageSize);
        taskParams.setPrefix(prefix);

        String result = taskParams.serialize();
        String cleanResult = result.replaceAll("\\s+", "");
        assertThat(cleanResult, containsString("\"snapshotId\":\""+ snapshotId +"\""));
        assertThat(cleanResult, containsString("\"pageNumber\":"+ pageNumber +""));
        assertThat(cleanResult, containsString("\"pageSize\":"+ pageSize +""));
        assertThat(cleanResult, containsString("\"prefix\":\""+ prefix +"\""));
    }

    @Test
    public void testDeserialize() {
        // Verify valid params
        String taskParamsSerialized =
            "{\"snapshotId\" : \""+snapshotId+"\"," +
            "\"pageNumber\" : "+pageNumber+"," +
            " \"pageSize\" : "+pageSize+"," +
            " \"prefix\" : \""+prefix+"\"}";

        GetSnapshotContentsTaskParameters taskParams =
            GetSnapshotContentsTaskParameters.deserialize(taskParamsSerialized);
        assertEquals(snapshotId, taskParams.getSnapshotId());
        assertEquals(pageNumber, taskParams.getPageNumber());
        assertEquals(pageSize, taskParams.getPageSize());
        assertEquals(prefix, taskParams.getPrefix());

        // Verify that empty params throw
        taskParamsSerialized = "{\"snapshotId\" : \"\"}";

        try {
            GetSnapshotContentsTaskParameters.deserialize(taskParamsSerialized);
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }

        // Verify that empty params throw
        try {
            GetSnapshotContentsTaskParameters.deserialize("");
            fail("Exception expected: Invalid params");
        } catch(SnapshotDataException e) {
        }
    }

}
