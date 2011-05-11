/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.storage.xml.impl;

import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.storage.xml.StorageAccountProviderBinding;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a simple xml binding for storage accounts.
 *
 * @author Andrew Woods
 *         Date: 5/9/11
 */
public class StorageAccountProviderSimpleBindingImpl implements StorageAccountProviderBinding {

    private final Logger log = LoggerFactory.getLogger(
        StorageAccountProviderSimpleBindingImpl.class);

    @Override
    public StorageAccount getAccountFromXml(Element xml) throws Exception {
        String type = xml.getChildText("storageProviderType");
        String storageAccountId = xml.getChildText("id");
        String primary = xml.getAttributeValue("isPrimary");

        String username = null;
        String password = null;
        Element credentials = xml.getChild("storageProviderCredential");
        if (null != credentials) {
            String encUsername = credentials.getChildText("username");
            String encPassword = credentials.getChildText("password");

            if (null != encUsername && null != encPassword) {
                EncryptionUtil encryptUtil = new EncryptionUtil();
                username = encryptUtil.decrypt(encUsername);
                password = encryptUtil.decrypt(encPassword);
            }
        }

        StorageProviderType acctType = StorageProviderType.fromString(type);
        StorageAccount storageAccount = null;
        if (storageAccountId != null && acctType != null && !acctType.equals(
            StorageProviderType.UNKNOWN)) {

            storageAccount = new StorageAccountImpl(storageAccountId,
                                                    username,
                                                    password,
                                                    acctType);

            storageAccount.setPrimary(isPrimary(primary));

        } else {
            log.warn("While creating storage account list, skipping storage " +
                         "account with storageAccountId '" + storageAccountId +
                         "' due to either a missing storageAccountId, " +
                         "an unsupported type '" + type + "', " +
                         "or a missing username or password");
        }

        return storageAccount;
    }

    private boolean isPrimary(String primary) {
        if (null == primary) {
            return false;
        }
        return primary.equals("1") || primary.equalsIgnoreCase("true");
    }
}
