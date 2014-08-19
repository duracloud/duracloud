/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.id;

import org.duracloud.common.util.DateUtil;

import java.text.ParseException;

/**
 * @author Bill Branan
 *         Date: 7/30/14
 */
public class SnapshotIdentifier {

    public static final String DELIM = "_";

    private String accountName;
    private String storeId;
    private String spaceId;
    private long timestamp;

    public SnapshotIdentifier(String accountName, String storeId,
                              String spaceId, long timestamp) {
        this.accountName = accountName;
        this.storeId = storeId;
        this.spaceId = spaceId;
        this.timestamp = timestamp;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSnapshotId() {
        return accountName + DELIM + storeId + DELIM + spaceId + DELIM +
               DateUtil.convertToStringPlain(timestamp);
    }

    public String getRestoreSpaceId() {
        String spaceName =
            spaceId + "-" + DateUtil.convertToStringPlain(timestamp);
        if(spaceName.length() > 42) { // Cut to 42 characters or less
            spaceName = spaceName.substring(0, 42);
        }
        if(spaceName.endsWith("-")) { // Remove trailing dash
            spaceName = spaceName.substring(0, spaceName.length()-1);
        }
        return spaceName;
    }

    public static SnapshotIdentifier parseSnapshotId(String snapshotId)
        throws ParseException {
        String[] snapshotIdParts = snapshotId.split(DELIM);
        long timestamp = DateUtil.convertToDate(snapshotIdParts[3],
                                                DateUtil.DateFormat.PLAIN_FORMAT)
                                 .getTime();
        return new SnapshotIdentifier(snapshotIdParts[0],
                                      snapshotIdParts[1],
                                      snapshotIdParts[2],
                                      timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SnapshotIdentifier that = (SnapshotIdentifier) o;

        // Timestamps which are consistent to the second are considered equal
        if (timestamp/1000 != that.timestamp/1000) {
            return false;
        }
        if (!accountName.equals(that.accountName)) {
            return false;
        }
        if (!spaceId.equals(that.spaceId)) {
            return false;
        }
        if (!storeId.equals(that.storeId)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = accountName.hashCode();
        result = 31 * result + storeId.hashCode();
        result = 31 * result + spaceId.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
