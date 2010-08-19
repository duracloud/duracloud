/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.storage.error.StorageException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
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

    public void initialize(InputStream accountXml) {
        this.initialize(accountXml, false);
    }

    /**
     * Parses xml to construct a listing of available storage accounts.
     * The ignoreCredentials flag is available to allow parsing of account
     * xml which does not include credential information, such as the
     * xml produced by the GET /stores REST method.
     *
     * @param accountXml
     * @param ignoreCredentials
     * @throws StorageException
     */
    public void initialize(InputStream accountXml, boolean ignoreCredentials)
        throws StorageException {
        storageAccounts = new HashMap<String, StorageAccount>();

        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(accountXml);
            Element accounts = doc.getRootElement();

            Iterator<?> accountList = accounts.getChildren().iterator();
            while(accountList.hasNext()) {
                Element account = (Element)accountList.next();

                String storageAccountId = account.getChildText("id");
                String type = account.getChildText("storageProviderType");
                String username = null;
                String password = null;
                if(!ignoreCredentials) {
                Element credentials = account.getChild("storageProviderCredential");
                    if(credentials != null) {
                        String encUsername = credentials.getChildText("username");
                        String encPassword = credentials.getChildText("password");

                        EncryptionUtil encryptUtil = new EncryptionUtil();
                        username = encryptUtil.decrypt(encUsername);
                        password = encryptUtil.decrypt(encPassword);
                    }
                }

                StorageProviderType storageAccountType =
                    StorageProviderType.fromString(type);
                StorageAccount storageAccount = null;
                if(storageAccountId != null &&
                   storageAccountType != null &&
                   !storageAccountType.equals(StorageProviderType.UNKNOWN) &&
                   (ignoreCredentials || (username != null && password != null))) {
                    storageAccount = new StorageAccount(storageAccountId,
                                                        username,
                                                        password,
                                                        storageAccountType);
                    storageAccounts.put(storageAccountId, storageAccount);
                }
                else {
                    log.warn("While creating storage account list, skipping storage " +
                    		 "account with storageAccountId '" + storageAccountId +
                             "' due to either a missing storageAccountId, " +
                             "an unsupported type '" + type + "', " +
                             "or a missing username or password");
                }

            String primary = account.getAttributeValue("isPrimary");
            if(primary != null) {
                if(primary.equalsIgnoreCase("1") ||
                   primary.equalsIgnoreCase("true"))
                    primaryStorageProviderId = storageAccountId;
                }
            }

            // Make sure that there is at least one storage account
            if(storageAccounts.isEmpty()) {
                String error = "No storage accounts could be read";
                throw new StorageException(error);
            } else {
                // Make sure a primary provider is set
                if(primaryStorageProviderId == null) {
                    primaryStorageProviderId =
                        storageAccounts.values().iterator().next().getId();
                }
            }
        } catch (Exception e) {
            String error = "Unable to build storage account information due " +
            		       "to error: " + e.getMessage();
            log.error(error);
            throw new StorageException(error, e);
        }
    }

    public String getPrimaryStorageAccountId() {
        checkInitialized();
        return primaryStorageProviderId;
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
