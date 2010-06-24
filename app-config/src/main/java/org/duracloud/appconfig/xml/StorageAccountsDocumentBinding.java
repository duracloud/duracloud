/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.xml;

import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class (de)serializes durastore acct configuration between objects and xml.
 *
 * @author Andrew Woods
 *         Date: Apr 21, 2010
 */
public class StorageAccountsDocumentBinding {
    private static final Logger log = LoggerFactory.getLogger(
        StorageAccountsDocumentBinding.class);

    private static EncryptionUtil encryptionUtil;

    /**
     * This method deserializes the provided xml into a durastore acct config object.
     *
     * @param xml
     * @return
     */
    public static List<StorageAccount> createStorageAccountsFrom(InputStream xml) {
        List<StorageAccount> accts = new ArrayList<StorageAccount>();
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(xml);
            Element accounts = doc.getRootElement();

            Iterator<?> accountList = accounts.getChildren().iterator();
            while (accountList.hasNext()) {
                Element account = (Element) accountList.next();

                String storageAccountId = account.getChildText("id");
                String type = account.getChildText("storageProviderType");
                String username = null;
                String password = null;
                Element credentials = account.getChild(
                    "storageProviderCredential");
                if (credentials != null) {
                    String encUsername = credentials.getChildText("username");
                    String encPassword = credentials.getChildText("password");

                    username = decrypt(encUsername);
                    password = decrypt(encPassword);
                }

                StorageProviderType storageAccountType = StorageProviderType.fromString(
                    type);
                StorageAccount acct = null;
                if (storageAccountId != null && storageAccountType != null &&
                    !storageAccountType.equals(StorageProviderType.UNKNOWN) &&
                    (username != null && password != null)) {
                    acct = new StorageAccount(storageAccountId,
                                              username,
                                              password,
                                              storageAccountType);
                    accts.add(acct);
                } else {
                    log.warn(
                        "While creating storage account list, skipping storage " +
                            "account with storageAccountId '" +
                            storageAccountId +
                            "' due to either a missing storageAccountId, " +
                            "an unsupported type '" + type + "', " +
                            "or a missing username or password");
                }
            }

            // Make sure that there is at least one storage account
            if (accts.isEmpty()) {
                String error = "No storage accounts could be read";
                throw new StorageException(error);
            }

        } catch (Exception e) {
            String error = "Unable to build storage account information due " +
                "to error: " + e.getMessage();
            log.error(error);
            throw new StorageException(error, e);
        }

        return accts;
    }

    /**
     * This method serializes the provide durastore acct configuration into xml.
     *
     * @param accts
     * @return
     */
    public static String createDocumentFrom(Collection<StorageAccount> accts) {
        StringBuilder xml = new StringBuilder();

        if (null != accts && accts.size() > 0) {
            xml.append("<storageProviderAccounts>");

            for (StorageAccount acct : accts) {
                String isPrimary = acct.isPrimary() ? "1" : "0";
                String username = encrypt(acct.getUsername());
                String password = encrypt(acct.getPassword());

                xml.append("  <storageAcct ownerId='0' isPrimary='");
                xml.append(isPrimary + "'>");
                xml.append("    <id>" + acct.getId() + "</id>");
                xml.append("    <storageProviderType>");
                xml.append(acct.getType().name() + "</storageProviderType>");
                xml.append("    <storageProviderCredential>");
                xml.append("      <username>" + username + "</username>");
                xml.append("      <password>" + password + "</password>");
                xml.append("    </storageProviderCredential>");
                xml.append("  </storageAcct>");
            }

            xml.append("</storageProviderAccounts>");
        }
        return xml.toString();
    }

    private static String encrypt(String text) {
        try {
            return getEncryptionUtil().encrypt(text);
        } catch (Exception e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    private static String decrypt(String text) {
        try {
            return getEncryptionUtil().decrypt(text);
        } catch (Exception e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    private static EncryptionUtil getEncryptionUtil() {
        if (null == encryptionUtil) {
            try {
                encryptionUtil = new EncryptionUtil();
            } catch (Exception e) {
                throw new DuraCloudRuntimeException(e);
            }
        }
        return encryptionUtil;
    }
}
