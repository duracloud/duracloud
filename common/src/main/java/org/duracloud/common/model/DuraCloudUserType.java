/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.model;

/**
 * @author Andrew Woods
 *         Date: Mar 15, 2010
 */
public enum DuraCloudUserType {

    ROOT("root-user"), SYSTEM("system-user"), ADMIN("admin-user"), USER(
        "standard-user"), ANONYMOUS("anonymous-user"), UNKNOWN("unknown-user");

    private final String text;

    private DuraCloudUserType(String ut) {
        text = ut;
    }

    public static DuraCloudUserType fromString(String ut) {
        for (DuraCloudUserType uType : values()) {
            if (uType.text.equalsIgnoreCase(ut) ||
                uType.name().equalsIgnoreCase(ut)) {
                return uType;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return text;
    }

}
