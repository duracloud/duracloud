/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

public class SpaceMessage extends ContentStoreMessage {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SpaceMessage[");
        sb.append("storeId:'" + getStoreId() + "'");
        sb.append("|spaceId:'" + getSpaceId() + "'");
        sb.append("|username:'" + getUsername() + "'");
        sb.append("]\n");
        return sb.toString();
    }

}
