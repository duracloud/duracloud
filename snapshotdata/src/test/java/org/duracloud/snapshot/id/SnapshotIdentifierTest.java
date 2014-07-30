/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.id;

import org.duracloud.common.util.DateUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * @author Bill Branan
 *         Date: 7/30/14
 */
public class SnapshotIdentifierTest {

    @Test
    public void testParse() throws Exception {
        String accountName = "account-name";
        String storeId = "store-id";
        String spaceId = "space-id";
        long timestamp = 1406744610;

        SnapshotIdentifier snapshotIdentifier =
            new SnapshotIdentifier(accountName, storeId, spaceId, timestamp);
        String snapshotId = snapshotIdentifier.getSnapshotId();
        assertThat(snapshotId, containsString(accountName));
        assertThat(snapshotId, containsString(storeId));
        assertThat(snapshotId, containsString(spaceId));
        assertThat(snapshotId,
                   containsString(DateUtil.convertToStringPlain(timestamp)));

        SnapshotIdentifier parsedIdentifier =
            SnapshotIdentifier.parseSnapshotId(snapshotId);
        assertTrue(snapshotIdentifier.equals(parsedIdentifier));
    }

    @Test
    public void testRestoreSpaceId() {
        String accountName = "account-name";
        String storeId = "store-id";
        long timestamp = 42l;

        String spaceId = "space-id";
        SnapshotIdentifier snapshotIdentifier =
            new SnapshotIdentifier(accountName, storeId, spaceId, timestamp);
        assertEquals(spaceId + "-" + DateUtil.convertToStringPlain(timestamp),
                     snapshotIdentifier.getRestoreSpaceId());

        spaceId = "unnecessarily-long-space-id";
        snapshotIdentifier =
            new SnapshotIdentifier(accountName, storeId, spaceId, timestamp);
        // This is verifying both that the space ID is truncated and that the
        // trailing dash is removed
        assertEquals(spaceId + "-" + DateUtil.convertToStringPlain(timestamp)
                                             .substring(0, 13),
                     snapshotIdentifier.getRestoreSpaceId());
    }

}
