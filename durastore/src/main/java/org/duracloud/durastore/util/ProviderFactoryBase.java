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
public abstract class ProviderFactoryBase {

    private static StorageAccountManager storageAccountManager;

    /**
     * Initializes DuraStore with account information
     * necessary to connect to Storage Providers.
     *
     * @param accountXml A stream containing account information in XML format
     */
    public static void initialize(InputStream accountXml)
            throws StorageException {
        if (accountXml == null) {
            throw new IllegalArgumentException("XML containing account information");
        }

        storageAccountManager = new StorageAccountManager(accountXml);
    }

    /**
     * Gets the account manager
     */
    protected static StorageAccountManager getAccountManager()
        throws StorageException {
        checkInitialized();
        return storageAccountManager;
    }

    /*
     * Ensures that the account manager has been initialized
     */
    private static void checkInitialized()
    throws StorageException {
        if (storageAccountManager == null) {
            String error =
                    "DuraStore must be initilized with an XML file " +
                    "containing storage account information before any " +
                    "further requests can be fulfilled.";
            throw new StorageException(error);
        }
    }

}
