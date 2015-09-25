/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.xml.impl;

import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Andrew Woods
 *         Date: 5/13/11
 */
public class StorageAccountProviderS3BindingImplTest {

    private StorageAccountProviderSimpleBindingImpl binding;

    @Before
    public void setUp() throws Exception {
        binding = new StorageAccountProviderSimpleBindingImpl();
    }

    @Test
    public void testGetAccountFromXmlNull() throws Exception {
        boolean valid = false;
        StorageAccount result = doTest(null, valid);
        Assert.assertNull(result);
    }

    @Test
    public void testGetAccountFromXmlBadCredentials() throws Exception {
        boolean include = true;
        String cfKeyId = null;
        StorageAccount acct = createAccount(cfKeyId);
        acct.setUsername(null);
        acct.setPassword(null);
        Element xml = createAccountXml(acct, include);

        boolean valid = false;
        StorageAccount result = doTest(xml, valid);
        Assert.assertNull(result);
    }

    @Test
    public void testGetAccountFromXmlUnknownProvider() throws Exception {
        boolean include = true;
        String cfKeyId = "cf-key-id";
        StorageAccount acct = createAccount(cfKeyId);
        acct.setType(StorageProviderType.UNKNOWN);
        Element xml = createAccountXml(acct, include);

        boolean valid = true;
        StorageAccount result = doTest(xml, valid);
        Assert.assertNull(result);
    }

    private StorageAccount doTest(Element xml, boolean valid) throws Exception {
        StorageAccount acct = null;

        boolean success = true;
        try {
            acct = binding.getAccountFromXml(xml);
            Assert.assertTrue("Exception expected", valid);

        } catch (Exception e) {
            Assert.assertFalse(e.getMessage(), valid);
            success = false;
        }
        Assert.assertEquals(valid, success);

        return acct;
    }

    private Element createAccountXml(StorageAccount acct,
                                     boolean includeCredentials)
        throws Exception {
        StringBuilder xml = new StringBuilder();

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

        String cfKeyId = acct.getOptions().get(StorageAccount.OPTS.CF_KEY_ID.name());

        xml.append("  <storageAcct ownerId='0' isPrimary='");
        xml.append(isPrimary + "'>");
        xml.append("    <id>" + acct.getId() + "</id>");

        if (null != cfKeyId && !"null".equals(cfKeyId)) {
            xml.append("    <storageProviderOptions>");
            xml.append("      <option name='");
            xml.append(StorageAccount.OPTS.CF_KEY_ID.name());
            xml.append("' value='"+cfKeyId+"' />");
            xml.append("    </storageProviderOptions>");
        }
        xml.append("    <storageProviderType>");
        xml.append(acct.getType().name() + "</storageProviderType>");

        if (includeCredentials) {
            xml.append("    <storageProviderCredential>");
            xml.append("      <username>" + username + "</username>");
            xml.append("      <password>" + password + "</password>");
            xml.append("    </storageProviderCredential>");
        }
        xml.append("  </storageAcct>");

        InputStream is = new ByteArrayInputStream(xml.toString().getBytes());
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(is);

        is.close();
        return doc.getRootElement();
    }

    private StorageAccount createAccount(String cfKeyId) {
        StorageAccount acct = null;

        String id = "id";
        String username = "username";
        String password = "password";
        acct = new StorageAccountImpl(id,
                                      username,
                                      password,
                                      StorageProviderType.AMAZON_S3);
        acct.setOption(StorageAccount.OPTS.CF_KEY_ID.name(), cfKeyId);

        return acct;
    }
}
