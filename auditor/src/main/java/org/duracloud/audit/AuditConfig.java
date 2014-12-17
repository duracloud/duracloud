/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit;

/**
 * Defines a set of parameters necessary for initializing audit log access.
 * 
 * @author Daniel Bernstein
 * 
 */
public class AuditConfig {
    private String logSpaceId;

    public String getLogSpaceId() {
        return logSpaceId;
    }

    public void setLogSpaceId(String logSpaceId) {
        this.logSpaceId = logSpaceId;
    }
}
