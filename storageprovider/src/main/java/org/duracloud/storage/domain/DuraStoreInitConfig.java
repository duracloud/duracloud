/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

import java.util.List;

/**
 * Configuration data used to initialize DuraStore
 *
 * @author Bill Branan
 *         Date: 3/18/14
 */
public class DuraStoreInitConfig {

    private AuditConfig auditConfig;
    private List<StorageAccount> storageAccounts;

    public AuditConfig getAuditConfig() {
        return auditConfig;
    }

    public void setAuditConfig(AuditConfig auditConfig) {
        this.auditConfig = auditConfig;
    }

    public List<StorageAccount> getStorageAccounts() {
        return storageAccounts;
    }

    public void setStorageAccounts(List<StorageAccount> storageAccounts) {
        this.storageAccounts = storageAccounts;
    }

}
