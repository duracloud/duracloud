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
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.duracloud.storage.xml.StorageAccountProviderBinding;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class provides a simple xml binding for storage accounts.
 *
 * @author Andrew Woods
 *         Date: 5/9/11
 */
public class StorageAccountProviderSimpleBindingImpl implements StorageAccountProviderBinding {

    private final Logger log = LoggerFactory.getLogger(
        StorageAccountProviderSimpleBindingImpl.class);

    private EncryptionUtil encryptionUtil;

    public StorageAccountProviderSimpleBindingImpl() {
        encryptionUtil = new EncryptionUtil();
    }

    @Override
    public StorageAccount getAccountFromXml(Element xml) {
        StorageProviderType acctType = getStorageProviderType(xml);
        String storageAccountId = getAccountId(xml);
        boolean primary = getIsPrimary(xml);
        Credential credential = getCredential(xml);
        Map<String, String> options = getStorageProviderOptions(xml);

        StorageAccount storageAccount = null;
        if (storageAccountId != null && acctType != null && !acctType.equals(
            StorageProviderType.UNKNOWN)) {

            storageAccount = new StorageAccountImpl(storageAccountId,
                                                    credential.getUsername(),
                                                    credential.getPassword(),
                                                    acctType);
            
            for (String key : options.keySet()) {
                storageAccount.setOption(key, options.get(key));
            }

            storageAccount.setPrimary(primary);

        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("While creating storage account list, skipping simple ");
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

    protected StorageProviderType getStorageProviderType(Element xml) {
        String type = xml.getChildText("storageProviderType");
        return StorageProviderType.fromString(type);
    }

    protected String getAccountId(Element xml) {
        return xml.getChildText("id");
    }

    protected boolean getIsPrimary(Element xml) {
        String primary = xml.getAttributeValue("isPrimary");
        if (null == primary) {
            return false;
        }
        return primary.equals("1") || primary.equalsIgnoreCase("true");
    }

    protected Credential getCredential(Element xml) {
        String username = null;
        String password = null;
        Element credentials = xml.getChild("storageProviderCredential");
        if (null != credentials) {
            String encUsername = credentials.getChildText("username");
            String encPassword = credentials.getChildText("password");

            if (null != encUsername && null != encPassword) {
                username = encryptionUtil.decrypt(encUsername);
                password = encryptionUtil.decrypt(encPassword);
            }
        }
        return new Credential(username, password);
    }

    private Map<String, String> getStorageProviderOptions(Element xml) {
        Map<String, String> options = new HashMap<String, String>();

        Element optionsXml = xml.getChild("storageProviderOptions");
        if (null != optionsXml) {
            Iterator<Element> itr = optionsXml.getChildren("option").iterator();
            while (itr.hasNext()) {
                Element optionXml = itr.next();
                String name = optionXml.getAttributeValue("name");
                String value = optionXml.getAttributeValue("value");

                options.put(name, value);
            }
        }
        return options;
    }

    @Override
    public Element getElementFrom(StorageAccount acct,
                                  boolean includeCredentials,
                                  boolean includeOptions) {

        Element storageAcct = new Element("storageAcct");
        storageAcct.setAttribute("ownerId", "0");

        String isPrimary = acct.isPrimary() ? "1" : "0";
        storageAcct.setAttribute("isPrimary", isPrimary);

        Element id = new Element("id");
        id.addContent(acct.getId());
        storageAcct.addContent(id);

        Element storageProviderType = new Element("storageProviderType");
        storageProviderType.addContent(acct.getType().name());
        storageAcct.addContent(storageProviderType);

        if (includeCredentials) {
            Element storageProviderCredential = new Element(
                "storageProviderCredential");

            String uname = encryptionUtil.encrypt(acct.getUsername());
            String pword = encryptionUtil.encrypt(acct.getPassword());

            Element username = new Element("username");
            Element password = new Element("password");

            username.addContent(uname);
            password.addContent(pword);

            storageProviderCredential.addContent(username);
            storageProviderCredential.addContent(password);

            storageAcct.addContent(storageProviderCredential);
        }

        if(includeOptions) {
            Map<String, String> options = acct.getOptions();
            if (null != options && !options.isEmpty()) {
                Element storageProviderOptions = new Element(
                    "storageProviderOptions");
                storageAcct.addContent(storageProviderOptions);

                for (String key : options.keySet()) {
                    Element option = new Element("option");
                    option.setAttribute("name", key);
                    option.setAttribute("value", options.get(key));

                    storageProviderOptions.addContent(option);
                }
            }
        }

        return storageAcct;
    }

}
