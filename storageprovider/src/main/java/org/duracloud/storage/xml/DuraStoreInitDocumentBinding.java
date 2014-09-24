/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.xml;

import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.storage.domain.AuditConfig;
import org.duracloud.storage.domain.DatabaseConfig;
import org.duracloud.storage.domain.DuraStoreInitConfig;
import org.duracloud.storage.error.StorageException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author Bill Branan
 *         Date: 3/18/14
 */
public class DuraStoreInitDocumentBinding {

    private final Logger log =
        LoggerFactory.getLogger(DuraStoreInitDocumentBinding.class);

    private StorageAccountsDocumentBinding accountsBinding;
    private EncryptionUtil encryptionUtil;

    public DuraStoreInitDocumentBinding() {
        accountsBinding = new StorageAccountsDocumentBinding();
        encryptionUtil = new EncryptionUtil();
    }

    /**
     * Deserializes the provided xml into durastore init config
     *
     * @param xml
     * @return
     */
    public DuraStoreInitConfig createFromXml(InputStream xml) {
        DuraStoreInitConfig initConfig = new DuraStoreInitConfig();
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(xml);
            Element root = doc.getRootElement();

            Element accounts = root.getChild("storageProviderAccounts");
            initConfig.setStorageAccounts(
                accountsBinding.createStorageAccountsFrom(accounts));

            Element audit = root.getChild("storageAudit");
            if(null != audit) {
                AuditConfig auditConfig = new AuditConfig();
                String encUser = audit.getChildText("auditUsername");
                if(null != encUser) {
                    auditConfig.setAuditUsername(encryptionUtil.decrypt(encUser));
                }
                String encPass = audit.getChildText("auditPassword");
                if(null != encPass) {
                    auditConfig.setAuditPassword(encryptionUtil.decrypt(encPass));
                }
                auditConfig.setAuditQueueName(audit.getChildText("auditQueue"));
                initConfig.setAuditConfig(auditConfig);
            }

            Element millDb = root.getChild("millDb");
            if(null != millDb) {
                DatabaseConfig millDbConfig = new DatabaseConfig();

                String host = millDb.getChildText("host");
                if(null != host) {
                    millDbConfig.setHost(host);
                }

                String port = millDb.getChildText("port");
                if(null != port) {
                    millDbConfig.setPort(Integer.parseInt(port));
                }

                String name = millDb.getChildText("name");
                if(null != name) {
                    millDbConfig.setName(name);
                }

                String username = millDb.getChildText("username");
                if(null != username) {
                    millDbConfig.setUsername(username);
                }

                String password = millDb.getChildText("password");
                if(null != password) {
                    millDbConfig.setPassword(encryptionUtil.decrypt(password));
                }

                initConfig.setMillDbConfig(millDbConfig);
            }

        } catch (Exception e) {
            String error = "Unable to build storage account information due " +
                "to error: " + e.getMessage();
            log.error(error);
            throw new StorageException(error, e);
        }
        return initConfig;
    }

    /**
     * Serializes the provided durastore init config into xml
     *
     * @param duraStoreInitConfig
     * @param includeCredentials
     * @return
     */
    public String createXmlFrom(DuraStoreInitConfig duraStoreInitConfig,
                                boolean includeCredentials,
                                boolean includeOptions) {
        String xml = "";

        Element durastoreConfig = new Element("durastoreConfig");

        Element accounts =
            accountsBinding.createDocumentFrom(
                duraStoreInitConfig.getStorageAccounts(),
                includeCredentials,
                includeOptions);
        durastoreConfig.addContent(accounts);

        Element audit = new Element("storageAudit");
        AuditConfig auditConfig = duraStoreInitConfig.getAuditConfig();

        String auditUsername = auditConfig.getAuditUsername();
        if(null != auditUsername) {
            String username = encryptionUtil.encrypt(auditUsername);
            audit.addContent(new Element("auditUsername").setText(username));
        }

        String auditPassword = auditConfig.getAuditPassword();
        if(null != auditPassword) {
            String password = encryptionUtil.encrypt(auditPassword);
            audit.addContent(new Element("auditPassword").setText(password));
        }

        String auditQueueName = auditConfig.getAuditQueueName();
        if(null != auditQueueName) {
            audit.addContent(new Element("auditQueue").setText(auditQueueName));
        }
        durastoreConfig.addContent(audit);

        
        Element millDb = new Element("millDb");
        DatabaseConfig millDbConfig = duraStoreInitConfig.getMillDbConfig();

        String host = millDbConfig.getHost();
        if(null != host) {
            millDb.addContent(new Element("host").setText(host));
        }

        int  port = millDbConfig.getPort();
        millDb.addContent(new Element("port").setText(port+""));

        String name = millDbConfig.getName();
        if(null != name) {
            millDb.addContent(new Element("name").setText(name));
        }

        String username = millDbConfig.getUsername();
        if(null != username) {
            millDb.addContent(new Element("username").setText(username));
        }

        String password = millDbConfig.getPassword();
        if(null != password) {
            millDb.addContent(new Element("password").setText(encryptionUtil.encrypt(password)));
        }

        durastoreConfig.addContent(millDb);

        
        Document document = new Document(durastoreConfig);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        return outputter.outputString(document);
    }
    

}
