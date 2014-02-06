/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.xml.StorageAccountsDocumentBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Manages storage provider accounts.
 *
 * @author Bill Branan
 */
public class StorageAccountManager {

    protected final Logger log = LoggerFactory.getLogger(StorageAccountManager.class);

    private String primaryStorageProviderId = null;
    private HashMap<String, StorageAccount> storageAccounts = null;

    private StorageAccountsDocumentBinding documentBinding =
        new StorageAccountsDocumentBinding();

    private String instanceHost;
    private String instancePort;

    /**
     * Parses xml to construct a listing of available storage accounts.
     * Closes the arg stream upon successful initialization.
     *
     * @param accountXml
     * @throws StorageException
     */
    public void initialize(InputStream accountXml)
        throws StorageException {

        List<StorageAccount> accts = getAccounts(accountXml);
        storageAccounts = new HashMap<String, StorageAccount>();
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

        close(accountXml);
    }

    public void setEnvironment(String instanceHost, String instancePort) {
        this.instanceHost = instanceHost;
        this.instancePort = instancePort;
    }

    public String getInstanceHost() {
        return instanceHost;
    }

    public String getInstancePort() {
        return instancePort;
    }

    private List<StorageAccount> getAccounts(InputStream accountXml) {
        List<StorageAccount> accts = null;
        try {
            accts = documentBinding.createStorageAccountsFrom(accountXml);

        } catch (Exception e) {
            String error = "Unable to build storage account information due " +
                "to error: " + e.getMessage();
            log.error(error);
            throw new StorageException(error, e);
        }

        if (null == accts || accts.isEmpty()) {
            String error = "Unable to build storage account information due " +
                "to invalid input xml.";
            log.error(error);
            throw new StorageException(error);
        }
        return accts;
    }

    private void close(InputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            // do nothing
        }
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
