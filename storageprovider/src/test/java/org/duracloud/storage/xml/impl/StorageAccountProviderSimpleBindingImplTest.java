/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.storage.xml.impl;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Andrew Woods
 *         Date: 5/11/11
 */
public class StorageAccountProviderSimpleBindingImplTest {

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
    public void testGetAccountFromXml() throws Exception {
        boolean include = true;
        StorageAccount acct = createAccount();
        Element xml = createAccountXml(acct, include);

        boolean valid = true;
        StorageAccount result = doTest(xml, valid);
        Assert.assertNotNull(result);
        Assert.assertEquals(acct, result);
    }

    @Test
    public void testGetAccountFromXml2() throws Exception {
        boolean include = false;
        StorageAccount acct = createAccount();
        Element xml = createAccountXml(acct, include);

        boolean valid = true;
        StorageAccount result = doTest(xml, valid);
        Assert.assertNotNull(result);
        Assert.assertEquals(acct.getId(), result.getId());
        Assert.assertEquals(acct.getOwnerId(), result.getOwnerId());
        Assert.assertEquals(acct.getType(), result.getType());
    }

    @Test
    public void testGetAccountFromXmlBadCredentials() throws Exception {
        boolean include = true;
        StorageAccount acct = createAccount();
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
        StorageAccount acct = createAccount();
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
            Assert.assertTrue("Exception not expected", valid);

        } catch (Exception e) {
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

        xml.append("  <storageAcct ownerId='0' isPrimary='");
        xml.append(isPrimary + "'>");
        xml.append("    <id>" + acct.getId() + "</id>");
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

    private StorageAccount createAccount() {
        String id = "id";
        String username = "username";
        String password = "password";
        StorageProviderType type = StorageProviderType.RACKSPACE;
        return new StorageAccountImpl(id, username, password, type);
    }
}
