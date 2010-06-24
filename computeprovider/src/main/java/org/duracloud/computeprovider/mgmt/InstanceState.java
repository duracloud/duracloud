/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.computeprovider.mgmt;

public enum InstanceState {

    RUNNING("running"),
    PENDING("pending"),
    TERMINATED("terminated"),
    SHUTTINGDOWN("shutting-down"),
    UNKNOWN("unknown");

    private final String text;

    private InstanceState(String s) {
        text = s;
    }

    public static InstanceState fromString(String s) {
        for (InstanceState state : values()) {
            if (state.text.equalsIgnoreCase(s)) {
                return state;
            }
        }
        return UNKNOWN;
    }

}
