/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import org.duracloud.snapshot.dto.RestoreStatus;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Daniel Bernstein
 *         Date: 7/29/14
 */
public class GetRestoreBridgeResultTest {

    private String restoreId = "restoreId";
    private String snapshotId = "snapshotId";
    private RestoreStatus status = RestoreStatus.RESTORATION_COMPLETE;
    private Date startDate  = new Date();
    private Date endDate  = new Date();
    private Date expirationDate = new Date();
    private String statusText = "status text";
    private String destinationHost = "dest-host";
    private int destinationPort = 5050;
    private String destinationStoreId = "dest-store-id";
    private String destinationSpaceId = "dest-space-id";

    @Test
    public void testDeSerialize(){
        String str =
            "{ \"restoreId\": \"" + restoreId + "\"," +
             " \"snapshotId\" : \"" + snapshotId + "\"," +
             " \"startDate\" : \"" + startDate.getTime() + "\"," +
             " \"endDate\" : \"" + endDate.getTime() + "\","  +
             " \"expirationDate\" : \"" + expirationDate.getTime() + "\","  +
             " \"status\" : \"" + status + "\"," +
             " \"statusText\" : \"" + statusText + "\"," +
             " \"destinationHost\" : \"" + destinationHost + "\"," +
             " \"destinationPort\" : " + destinationPort + "," +
             " \"destinationStoreId\" : \"" + destinationStoreId + "\"," +
             " \"destinationSpaceId\" : \"" + destinationSpaceId + "\"" +
             "}";

        GetRestoreBridgeResult params = GetRestoreBridgeResult
            .deserialize(str);

        assertEquals(restoreId, params.getRestoreId());
        assertEquals(snapshotId, params.getSnapshotId());
        assertEquals(status, params.getStatus());
        assertEquals(startDate, params.getStartDate());
        assertEquals(endDate, params.getEndDate());
        assertEquals(expirationDate, params.getExpirationDate());
        assertEquals(statusText, params.getStatusText());
        assertEquals(destinationHost, params.getDestinationHost());
        assertEquals(destinationPort, params.getDestinationPort());
        assertEquals(destinationStoreId, params.getDestinationStoreId());
        assertEquals(destinationSpaceId, params.getDestinationSpaceId());
    }

    @Test
    public void testToString() {
        GetRestoreBridgeResult result = new GetRestoreBridgeResult();
        result.setRestoreId(restoreId);
        result.setSnapshotId(snapshotId);
        result.setStatus(status);
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setExpirationDate(expirationDate);
        result.setStatusText(statusText);
        result.setDestinationHost(destinationHost);
        result.setDestinationPort(destinationPort);
        result.setDestinationStoreId(destinationStoreId);
        result.setDestinationSpaceId(destinationSpaceId);

        String value = result.toString();
        assertThat(value, containsString("restoreId=" + restoreId));
        assertThat(value, containsString("snapshotId=" + snapshotId));
        assertThat(value, containsString("status=" + status.name()));
        assertThat(value, containsString("startDate=" + startDate));
        assertThat(value, containsString("endDate=" + endDate));
        assertThat(value, containsString("expirationDate=" + expirationDate));
        assertThat(value, containsString("statusText=" + statusText));
        assertThat(value, containsString("destinationHost=" + destinationHost));
        assertThat(value, containsString("destinationPort=" + destinationPort));
        assertThat(value,
                   containsString("destinationStoreId=" + destinationStoreId));
        assertThat(value,
                   containsString("destinationSpaceId=" + destinationSpaceId));
    }

}
