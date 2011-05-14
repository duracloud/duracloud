/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.xml.impl;

import org.duracloud.common.model.Credential;
import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.storage.domain.impl.StorageAccountS3Impl;
import org.duracloud.storage.xml.StorageAccountProviderBinding;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a simple xml binding for S3 storage accounts.
 *
 * @author Andrew Woods
 *         Date: 5/11/11
 */
public class StorageAccountProviderS3BindingImpl extends StorageAccountProviderSimpleBindingImpl {

    private final Logger log = LoggerFactory.getLogger(
        StorageAccountProviderS3BindingImpl.class);

    @Override
    public StorageAccount getAccountFromXml(Element xml) {
        StorageProviderType acctType = getStorageProviderType(xml);
        String storageAccountId = getAccountId(xml);
        boolean primary = getIsPrimary(xml);
        Credential credential = getCredential(xml);
        String storageClass = getStorageClass(xml);

        StorageAccount storageAccount = null;
        if (storageAccountId != null && acctType != null && acctType.equals(
            StorageProviderType.AMAZON_S3)) {

            storageAccount = new StorageAccountS3Impl(storageAccountId,
                                                      credential.getUsername(),
                                                      credential.getPassword(),
                                                      storageClass);

            storageAccount.setPrimary(primary);

        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("While creating storage account list, skipping S3 ");
            sb.append("storage account with storageAccountId '");
            sb.append(storageAccountId);
            sb.append("' due to either a missing storageAccountId, ");
            sb.append("an unsupported type '");
            sb.append(acctType);
            sb.append("', or a missing username or password");
            log.warn(sb.toString());
        }

        return storageAccount;
    }

    private String getStorageClass(Element xml) {
        return xml.getChildText("storageClass");
    }

    @Override
    public Element getElementFrom(StorageAccount acct,
                                  boolean includeCredentials) {
        Element storageAcct = super.getElementFrom(acct, includeCredentials);

        String prop = acct.getProperty(StorageAccount.PROPS
                                           .STORAGE_CLASS
                                           .name());

        Element storageClass = new Element("storageClass");
        storageClass.addContent(prop);
        storageAcct.addContent(storageClass);

        return storageAcct;
    }
}
