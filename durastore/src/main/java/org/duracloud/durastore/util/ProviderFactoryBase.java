/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.error.StorageException;

import java.io.InputStream;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class ProviderFactoryBase {

    private StorageAccountManager storageAccountManager;

    public ProviderFactoryBase(StorageAccountManager storageAccountManager) {
        this.storageAccountManager = storageAccountManager;
    }

    /**
     * Initializes DuraStore with account information
     * necessary to connect to Storage Providers.
     *
     * @param accountXml A stream containing account information in XML format
     */
    public void initialize(InputStream accountXml,
                           String instanceHost,
                           String instancePort)
            throws StorageException {
        if (accountXml == null) {
            throw new IllegalArgumentException("XML containing account information");
        }

        storageAccountManager.initialize(accountXml);
        storageAccountManager.setEnvironment(instanceHost, instancePort);
    }

    /**
     * Gets the account manager
     */
    protected StorageAccountManager getAccountManager()
        throws StorageException {
        checkInitialized();
        return storageAccountManager;
    }

    public boolean isInitialized() {
        try {
            checkInitialized();
            return true;
        } catch(StorageException e) {
            return false;
        }
    }

    /*
     * Ensures that the account manager has been initialized
     */
    private void checkInitialized()
    throws StorageException {
        if (storageAccountManager == null || !storageAccountManager.isInitialized()) {
            String error =
                    "DuraStore must be initialized with an XML file " +
                    "containing storage account information before any " +
                    "further requests can be fulfilled.";
            throw new StorageException(error);
        }
    }

}
