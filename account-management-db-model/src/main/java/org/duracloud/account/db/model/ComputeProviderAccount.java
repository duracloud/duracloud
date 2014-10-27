/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model;

import org.duracloud.computeprovider.domain.ComputeProviderType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author Erik Paulsson
 *         Date: 7/10/13
 */
@Entity
public class ComputeProviderAccount extends ProviderAccount {

    /**
     * The type of compute provider - meaning the organization acting as the
     * provider of compute services.
     */
    @Enumerated(EnumType.STRING)
    private ComputeProviderType providerType;

    /**
     * The IP address to which the instance host for this compute account
     * will be attached
     */
    private String elasticIp;

    /**
     * The grouping of firewall paramters which will be applied to the
     * instance managed by this compute account
     */
    private String securityGroup;

    /**
     * The key pair which will be used to directly access the server instance
     * managed by this compute account
     */
    private String keypair;
    
    /**
     * The name of the audit queue to be associated with DuraStore.
     */
    private String auditQueue;

    public ComputeProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(ComputeProviderType providerType) {
        this.providerType = providerType;
    }

    public String getElasticIp() {
        return elasticIp;
    }

    public void setElasticIp(String elasticIp) {
        this.elasticIp = elasticIp;
    }

    public String getSecurityGroup() {
        return securityGroup;
    }

    public void setSecurityGroup(String securityGroup) {
        this.securityGroup = securityGroup;
    }

    public String getKeypair() {
        return keypair;
    }

    public void setKeypair(String keypair) {
        this.keypair = keypair;
    }

    public String getAuditQueue() {
        return auditQueue;
    }

    public void setAuditQueue(String auditQueue) {
        this.auditQueue = auditQueue;
    }
}
