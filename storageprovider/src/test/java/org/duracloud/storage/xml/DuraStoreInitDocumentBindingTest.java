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
import org.duracloud.storage.domain.DuraStoreInitConfig;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Bill Branan
 *         Date: 3/18/14
 */
public class DuraStoreInitDocumentBindingTest {

    private DuraStoreInitDocumentBinding binding;
    private EncryptionUtil encryptionUtil;
    private String username = "username";
    private String password = "password";
    private String queueName = "queue";

    @Before
    public void setup() {
        binding = new DuraStoreInitDocumentBinding();
        encryptionUtil = new EncryptionUtil();
    }

    @Test
    public void testCreateCycle() throws Exception {
        String xml = createXml();
        InputStream xmlStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        DuraStoreInitConfig initConfig = binding.createFromXml(xmlStream);

        verifyDuraStoreInitConfig(initConfig);

        String xmlVersion2 = binding.createXmlFrom(initConfig, true);
        InputStream xmlStreamVersion2 =
            new ByteArrayInputStream(xmlVersion2.getBytes("UTF-8"));
        DuraStoreInitConfig initConfigVersion2 =
            binding.createFromXml(xmlStreamVersion2);

        verifyDuraStoreInitConfig(initConfigVersion2);
    }

    private void verifyDuraStoreInitConfig(DuraStoreInitConfig initConfig) {
        AuditConfig auditConfig = initConfig.getAuditConfig();
        assertEquals(username, auditConfig.getAuditUsername());
        assertEquals(password, auditConfig.getAuditPassword());
        assertEquals(queueName, auditConfig.getAuditQueueName());

        List<StorageAccount> accounts = initConfig.getStorageAccounts();
        assertEquals(1, accounts.size());
        StorageAccount account = accounts.get(0);
        assertEquals(StorageProviderType.AMAZON_S3, account.getType());
        assertEquals("0", account.getId());
        assertEquals("rrs", account.getOptions().get("STORAGE_CLASS"));
        assertEquals(username, account.getUsername());
        assertEquals(password, account.getPassword());
    }

    private String createXml() {
        String encUser = encryptionUtil.encrypt(username);
        String encPass = encryptionUtil.encrypt(password);

        StringBuilder acctXml = new StringBuilder();
        acctXml.append("<durastoreConfig>");
        acctXml.append("  <storageAudit>");
        acctXml.append("    <auditUsername>" + encUser + "</auditUsername>");
        acctXml.append("    <auditPassword>" + encPass + "</auditPassword>");
        acctXml.append("    <auditQueue>" + queueName + "</auditQueue>");
        acctXml.append("  </storageAudit>");
        acctXml.append("  <storageProviderAccounts>");
        acctXml.append("    <storageAcct ownerId='0' isPrimary='true'>");
        acctXml.append("      <id>0</id>");
        acctXml.append("      <storageProviderType>" +
                              StorageProviderType.AMAZON_S3 +
                              "</storageProviderType>");
        acctXml.append("      <storageProviderOptions>");
        acctXml.append("        <option name='STORAGE_CLASS' value='rrs' />");
        acctXml.append("      </storageProviderOptions>");
        acctXml.append("      <storageProviderCredential>");
        acctXml.append("        <username>" + encUser + "</username>");
        acctXml.append("        <password>" + encPass + "</password>");
        acctXml.append("      </storageProviderCredential>");
        acctXml.append("    </storageAcct>");
        acctXml.append("  </storageProviderAccounts>");
        acctXml.append("</durastoreConfig>");

        return acctXml.toString();
    }
}
