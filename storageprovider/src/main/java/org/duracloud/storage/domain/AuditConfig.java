/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

/**
 * @author Bill Branan
 *         Date: 3/18/14
 */
public class AuditConfig {

    private String auditUsername;
    private String auditPassword;
    private String auditQueueName;
    private String auditLogSpaceId;

    public String getAuditUsername() {
        return auditUsername;
    }

    public void setAuditUsername(String auditUsername) {
        this.auditUsername = auditUsername;
    }

    public String getAuditPassword() {
        return auditPassword;
    }

    public void setAuditPassword(String auditPassword) {
        this.auditPassword = auditPassword;
    }

    public String getAuditQueueName() {
        return auditQueueName;
    }

    public void setAuditQueueName(String auditQueueName) {
        this.auditQueueName = auditQueueName;
    }

    public String getAuditLogSpaceId() {
        return auditLogSpaceId;
    }

    public void setAuditLogSpaceId(String auditLogSpaceId) {
        this.auditLogSpaceId = auditLogSpaceId;
    }

}
