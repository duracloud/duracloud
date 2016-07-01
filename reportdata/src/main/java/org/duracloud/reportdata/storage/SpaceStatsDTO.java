package org.duracloud.reportdata.storage;

/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

import java.util.Date;


/**
 * Represents a space's byte and object count at a moment in time.
 * @author Daniel Bernstein
 *
 */
public class SpaceStatsDTO  extends StoreStatsDTO {
    private String spaceId;
    
    public SpaceStatsDTO (){}
    
    public SpaceStatsDTO(Date timestamp, String account, String storeId, String spaceId, long byteCount, long objectCount) {
        super(timestamp, account,storeId, byteCount,objectCount);
        setSpaceId(spaceId);
    }
    
    public String getSpaceId() {
        return spaceId;
    }
    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }
}
