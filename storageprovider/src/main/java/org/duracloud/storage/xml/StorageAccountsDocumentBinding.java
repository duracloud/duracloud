/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.xml;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.xml.impl.StorageAccountProviderSimpleBindingImpl;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class (de)serializes durastore acct configuration between objects and xml.
 *
 * @author Andrew Woods
 *         Date: Apr 21, 2010
 */
public class StorageAccountsDocumentBinding {
    private final Logger log = LoggerFactory.getLogger(
        StorageAccountsDocumentBinding.class);

    private Map<StorageProviderType, StorageAccountProviderBinding> providerBindings;

    public StorageAccountsDocumentBinding() {
        providerBindings = new HashMap<StorageProviderType, StorageAccountProviderBinding>();

        // This map should have provider-specific implementations of
        //  StorageAccountProviderBinding, as necessary.
        for (StorageProviderType type : StorageProviderType.values()) {
            StorageAccountProviderBinding binding = new StorageAccountProviderSimpleBindingImpl();
            providerBindings.put(type, binding);
        }
    }

    /**
     * This method deserializes the provided xml into a durastore acct config object.
     *
     * @param accounts
     * @return
     */
    public List<StorageAccount> createStorageAccountsFrom(Element accounts) {
        List<StorageAccount> accts = new ArrayList<StorageAccount>();
        try {
            Iterator<?> accountList = accounts.getChildren().iterator();
            while (accountList.hasNext()) {
                Element accountXml = (Element) accountList.next();

                String type = accountXml.getChildText("storageProviderType");
                StorageProviderType acctType = StorageProviderType.fromString(
                    type);

                StorageAccountProviderBinding providerBinding = providerBindings
                    .get(acctType);
                if (null != providerBinding) {
                    accts.add(providerBinding.getAccountFromXml(accountXml));

                } else {
                    log.warn("Unexpected account type: " + acctType);
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
     * Creates storage accounts listing from XML which includes only the
     * storage accounts list (not the full DuraStore config. This is used
     * to parse the response of the GET stores DuraStore REST call.
     *
     * @param xml
     * @return
     */
    public List<StorageAccount> createStorageAccountsFromXml(InputStream xml) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(xml);
            Element root = doc.getRootElement();
            return createStorageAccountsFrom(root);
        } catch(Exception e) {
            String error = "Could not build storage accounts from xml " +
                           "due to: " + e.getMessage();
            throw new DuraCloudRuntimeException(error, e);
        }
    }

    /**
     * Converts the provided DuraStore acct configuration into an Xml element,
     * to be included in a larger Xml document (likely DuraStore config)
     *
     * @param accts
     * @return
     */
    public Element createDocumentFrom(Collection<StorageAccount> accts,
                                      boolean includeCredentials,
                                      boolean includeOptions) {
        Element storageProviderAccounts = new Element("storageProviderAccounts");

        if (null != accts && accts.size() > 0) {
            for (StorageAccount acct : accts) {

                StorageAccountProviderBinding providerBinding =
                    providerBindings.get(acct.getType());
                if (null != providerBinding) {
                    storageProviderAccounts.addContent(
                        providerBinding.getElementFrom(acct,
                                                       includeCredentials,
                                                       includeOptions));

                } else {
                    log.warn("Unexpected account type: " + acct.getType());
                }
            }
        }
        return storageProviderAccounts;
    }

    /**
     * Converts the provided DuraStore acct configuration into a stand-alone
     * XML document. This is used for the DuraStore GET stores REST call.
     *
     * @param accts
     * @return
     */
    public String createXmlFrom(Collection<StorageAccount> accts,
                                boolean includeCredentials,
                                boolean includeOptions) {
        Element storageProviderAccounts =
            createDocumentFrom(accts, includeCredentials, includeOptions);
        Document document = new Document(storageProviderAccounts);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        return outputter.outputString(document);
    }

}
