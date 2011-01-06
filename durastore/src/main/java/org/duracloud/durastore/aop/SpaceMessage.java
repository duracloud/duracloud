/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

public class SpaceMessage {

    private String storeId;

    private String spaceId;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SpaceMessage[");
        sb.append("storeId:'" + storeId + "'");
        sb.append("|spaceId:'" + spaceId + "'");
        sb.append("]\n");
        return sb.toString();
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }
}
