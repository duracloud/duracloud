/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import org.duracloud.storage.domain.DuraStoreInitConfig;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.util.InitConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public abstract class ProviderFactoryBase  {

    private Logger log =
        LoggerFactory.getLogger(ProviderFactoryBase.class);

    private StorageAccountManager storageAccountManager;
    private DuraStoreInitConfig initConfig;

    public ProviderFactoryBase(StorageAccountManager storageAccountManager) {
        this.storageAccountManager = storageAccountManager;
    }

    /**
     * Initializes DuraStore with account information
     * necessary to connect to Storage Providers.
     *
     * @param initXml A stream containing account information in XML format
     */
    @Deprecated
    public void initialize(InputStream initXml,
                           String instanceHost,
                           String instancePort)
            throws StorageException {
        if (initXml == null) {
            throw new IllegalArgumentException("XML containing account information");
        }
        DuraStoreInitConfig initConfig = InitConfigParser.parseInitXml(initXml);
        initialize(initConfig, instanceHost, instancePort);
    }

    /**
     * Initializes DuraStore with account information
     * necessary to connect to Storage Providers.
     *
     * @param initXml A stream containing account information in XML format
     */
    public void initialize(DuraStoreInitConfig initConfig,
                           String instanceHost,
                           String instancePort)
            throws StorageException {
        this.initConfig = initConfig;
        storageAccountManager.initialize(initConfig.getStorageAccounts());
        storageAccountManager.setEnvironment(instanceHost, instancePort);
    }


    /**
     * @return the account manager
     * @throws StorageException if not initialized
     */
    protected StorageAccountManager getAccountManager()
        throws StorageException {
        checkInitialized();
        return storageAccountManager;
    }

    /**
     * @return the DuraStore init config
     * @throws StorageException if not initialized
     */
    protected DuraStoreInitConfig getInitConfig() throws StorageException {
        checkInitialized();
        return initConfig;
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
