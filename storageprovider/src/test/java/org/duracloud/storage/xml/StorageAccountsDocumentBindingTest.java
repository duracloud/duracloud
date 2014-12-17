/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.xml;

import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 5/13/11
 */
public class StorageAccountsDocumentBindingTest {

    private StorageAccountsDocumentBinding documentBinding;

    private final String storeClass = "reducedredundancy";
    private InputStream xml;

    @Before
    public void setUp() throws Exception {
        documentBinding = new StorageAccountsDocumentBinding();
    }

    @After
    public void tearDown() throws Exception {
        if (null != xml) {
            xml.close();
        }
    }

    @Test
    public void testCreateStorageAccountsFrom() throws Exception {
        InputStream xml = createXml();
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(xml);
        Element accountsElem = doc.getRootElement();

        List<StorageAccount> accts =
            documentBinding.createStorageAccountsFrom(accountsElem);

        verifyAccounts(accts);
    }

    private void verifyAccounts(List<StorageAccount> accts) {
        Assert.assertNotNull(accts);
        Assert.assertEquals(2, accts.size());
        for (StorageAccount acct : accts) {
            if (acct.getType() != StorageProviderType.AMAZON_S3 &&
                acct.getType() != StorageProviderType.RACKSPACE) {
                Assert.fail("Invalid StorageProviderType: " + acct.getType());
            }
            verifyAccount(acct);
        }
    }

    private void verifyAccount(StorageAccount acct) {
        Class clazz = acct.getClass();
        Assert.assertTrue(StorageAccountImpl.class.isAssignableFrom(clazz));

        Map<String, String> options = acct.getOptions();
        if (acct.getType() == StorageProviderType.AMAZON_S3) {
            String storageClass = options.get(StorageAccount.OPTS
                                                  .STORAGE_CLASS
                                                  .name());
            Assert.assertNotNull(storageClass);

        } else {
            Assert.assertEquals(0, options.size());
        }

        String id = acct.getId();
        String ownerId = acct.getOwnerId();
        String username = acct.getUsername();
        String password = acct.getPassword();

        Assert.assertNotNull(id);
        Assert.assertNotNull(ownerId);
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);

        Assert.assertEquals("id", id);
        Assert.assertEquals("0", ownerId);
        Assert.assertEquals("username", username);
        Assert.assertEquals("password", password);
    }

    @Test
    public void testCreateDocumentFrom() throws Exception {
        StorageAccount acct0 = createAccount(StorageProviderType.AMAZON_S3);
        StorageAccount acct1 = createAccount(StorageProviderType.RACKSPACE);

        List<StorageAccount> accts = new ArrayList<StorageAccount>();
        accts.add(acct0);
        accts.add(acct1);

        boolean includeCredentials = true;
        boolean includeOptions = true;
        Element accountsElem =
            documentBinding.createDocumentFrom(accts,
                                               includeCredentials,
                                               includeOptions);

        Assert.assertNotNull(accountsElem);
        List<StorageAccount> accounts =
            documentBinding.createStorageAccountsFrom(accountsElem);
        verifyAccounts(accounts);
    }

    private InputStream createXml() throws Exception {
        StringBuilder acct0 = createAccountXml(createAccount(StorageProviderType.RACKSPACE));
        StringBuilder acct1 = createAccountXml(createAccount(StorageProviderType.AMAZON_S3));

        StringBuilder accts = new StringBuilder();
        accts.append("<storageProviderAccounts>");
        accts.append(acct0);
        accts.append(acct1);
        accts.append("</storageProviderAccounts>");

        xml = new ByteArrayInputStream(accts.toString().getBytes());
        return xml;
    }

    private StringBuilder createAccountXml(StorageAccount acct)
        throws Exception {
        StringBuilder acctXml = new StringBuilder();

        String isPrimary = acct.isPrimary() ? "1" : "0";

        EncryptionUtil encryptionUtil = new EncryptionUtil();
        String username = acct.getUsername();
        String password = acct.getPassword();
        if (null != username) {
            username = encryptionUtil.encrypt(acct.getUsername());
        }
        if (null != password) {
            password = encryptionUtil.encrypt(acct.getPassword());
        }

        String storageClass = getStorageClass(acct);

        acctXml.append("  <storageAcct ownerId='0' isPrimary='");
        acctXml.append(isPrimary + "'>");
        acctXml.append("    <id>" + acct.getId() + "</id>");


        acctXml.append("    <storageProviderType>");
        acctXml.append(acct.getType().name() + "</storageProviderType>");
        if (null != storageClass) {
            acctXml.append("    <storageProviderOptions>");
            acctXml.append("      <option name='");
            acctXml.append(StorageAccount.OPTS.STORAGE_CLASS.name());
            acctXml.append("' value='"+storageClass+"' />");
            acctXml.append("    </storageProviderOptions>");
        }
        acctXml.append("    <storageProviderCredential>");
        acctXml.append("      <username>" + username + "</username>");
        acctXml.append("      <password>" + password + "</password>");
        acctXml.append("    </storageProviderCredential>");
        acctXml.append("  </storageAcct>");

        return acctXml;
    }

    private String getStorageClass(StorageAccount acct) {
        if (acct.getType() == StorageProviderType.AMAZON_S3) {
            return acct.getOptions().get(StorageAccount.OPTS
                                             .STORAGE_CLASS
                                             .name());
        }
        return null;
    }

    private StorageAccount createAccount(StorageProviderType type) {
        StorageAccount acct = null;

        String id = "id";
        String username = "username";
        String password = "password";

        acct = new StorageAccountImpl(id, username, password, type);

        if (type == StorageProviderType.AMAZON_S3) {
            acct.setOption(StorageAccount.OPTS.STORAGE_CLASS.name(), storeClass);
        }

        return acct;
    }

}
