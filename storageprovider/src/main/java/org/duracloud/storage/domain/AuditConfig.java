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
 * Date: 3/18/14
 */
public class AuditConfig {

    private String auditQueueName;
    private String auditLogSpaceId;
    private String auditQueueType;
    private String rabbitmqHost;
    private String rabbitmqExchange;
    private String rabbitmqUsername;
    private String rabbitmqPassword;

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

    public String getAuditQueueType() {

        return auditQueueType;
    }

    public void setAuditQueueType(String auditQueueType) {

        this.auditQueueType = auditQueueType;
    }

    public String getRabbitmqHost() {
        return rabbitmqHost;
    }

    public void setRabbitmqHost(String rabbitmqHost) {
        this.rabbitmqHost = rabbitmqHost;
    }

    public String getRabbitmqExchange() {
        return rabbitmqExchange;
    }

    public void setRabbitmqExchange(String rabbitmqExchange) {
        this.rabbitmqExchange = rabbitmqExchange;
    }

    public String getRabbitmqUsername() {
        return rabbitmqUsername;
    }

    public void setRabbitmqUsername(String rabbitmqUsername) {
        this.rabbitmqUsername = rabbitmqUsername;
    }

    public String getRabbitmqPassword() {
        return rabbitmqPassword;
    }

    public void setRabbitmqPassword(String rabbitmqPassword) {
        this.rabbitmqPassword = rabbitmqPassword;
    }

}
