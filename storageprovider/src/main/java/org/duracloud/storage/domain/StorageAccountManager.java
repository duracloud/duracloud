/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.duracloud.storage.error.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages storage provider accounts.
 *
 * @author Bill Branan
 */
public class StorageAccountManager {

    protected final Logger log = LoggerFactory.getLogger(StorageAccountManager.class);

    private String primaryStorageProviderId = null;
    private HashMap<String, StorageAccount> storageAccounts = null;

    private String instanceHost;
    private String instancePort;
    private String accountName;

    /**
     * Initializes the account manager based on provided accounts
     *
     * @param accts
     * @throws StorageException
     */
    public void initialize(List<StorageAccount> accts)
        throws StorageException {

        storageAccounts = new HashMap<>();
        for (StorageAccount acct : accts) {
            storageAccounts.put(acct.getId(), acct);
            if (acct.isPrimary()) {
                primaryStorageProviderId = acct.getId();
            }
        }

        // Make sure a primary provider is set
        if (primaryStorageProviderId == null) {
            primaryStorageProviderId = accts.get(0).getId();
        }
    }

    public void setEnvironment(String instanceHost, String instancePort, String accountName) {
        this.instanceHost = instanceHost;
        this.instancePort = instancePort;
        this.accountName = accountName;
    }

    public String getInstanceHost() {
        return instanceHost;
    }

    public String getInstancePort() {
        return instancePort;
    }

    public String getAccountName() {
        return accountName;
    }

    public StorageAccount getPrimaryStorageAccount() {
        checkInitialized();
        return getStorageAccount(primaryStorageProviderId);
    }

    public Iterator<String> getStorageAccountIds() {
        checkInitialized();
        return storageAccounts.keySet().iterator();
    }

    public StorageAccount getStorageAccount(String storageProviderId) {
        checkInitialized();
        return storageAccounts.get(storageProviderId);
    }

    public Map<String, StorageAccount> getStorageAccounts() {
        checkInitialized();
        return storageAccounts;
    }

    private void checkInitialized() throws StorageException {
        if (!isInitialized()) {
            String error =
                "DuraStore's StorageAccountManager must be initialized " +
                    "with an XML file containing storage account information " +
                    "before any further requests can be fulfilled.";
            throw new StorageException(error);
        }
    }

    public boolean isInitialized() throws StorageException {
        return (null != storageAccounts && null != primaryStorageProviderId);
    }

}
