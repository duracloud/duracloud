/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.xml.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

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

/**
 * @author Andrew Woods
 * Date: 5/11/11
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
        StorageProviderType type = StorageProviderType.AMAZON_GLACIER;
        return new StorageAccountImpl(id, username, password, type);
    }

    @Test
    public void testGetElementFrom() {
        EncryptionUtil encryptionUtil = new EncryptionUtil();

        String storeId = "store-id";
        String username = "username";
        String password = "password";
        StorageProviderType type = StorageProviderType.AMAZON_S3;
        String hiddenOptionName = StorageAccount.OPTS.CF_KEY_ID.name();
        String hiddenOptionValue = "option-value";
        String openOptionName = StorageAccount.OPTS.WRITABLE.name();
        String openOptionValue = "option-value";

        StorageAccount account =
            new StorageAccountImpl(storeId, username, password, type);
        account.setOption(hiddenOptionName, hiddenOptionValue);
        account.setOption(openOptionName, openOptionValue);

        // Include both credentials and options
        Element element = binding.getElementFrom(account, true, true);
        Assert.assertEquals(storeId, element.getChildText("id"));
        Assert.assertEquals(encryptionUtil.encrypt(username),
                            element.getChild("storageProviderCredential")
                                   .getChildText("username"));
        Assert.assertEquals(encryptionUtil.encrypt(password),
                            element.getChild("storageProviderCredential")
                                   .getChildText("password"));
        List<Element> options =
            element.getChild("storageProviderOptions").getChildren();
        Assert.assertEquals(2, options.size());

        verifyThatOptionIsContained(options, hiddenOptionName, hiddenOptionValue);
        verifyThatOptionIsContained(options, openOptionName, openOptionValue);

        // Include options but no credentials
        element = binding.getElementFrom(account, false, true);
        Assert.assertEquals(storeId, element.getChildText("id"));
        Assert.assertNull(element.getChild("storageProviderCredential"));
        options =
            element.getChild("storageProviderOptions").getChildren();
        Assert.assertEquals(2, options.size());

        verifyThatOptionIsContained(options, hiddenOptionName, hiddenOptionValue);
        verifyThatOptionIsContained(options, openOptionName, openOptionValue);

        // Include no credentials or options
        element = binding.getElementFrom(account, false, false);
        Assert.assertEquals(storeId, element.getChildText("id"));
        Assert.assertNull(element.getChild("storageProviderCredential"));
        options =
            element.getChild("storageProviderOptions").getChildren();
        Assert.assertEquals(1, options.size());
        verifyThatOptionIsContained(options, openOptionName, openOptionValue);

    }

    private void verifyThatOptionIsContained(List<Element> options, String name, String value) {
        Assert.assertEquals(1, options.stream()
                                      .filter(option -> option.getAttribute("name").getValue().equals(name))
                                      .filter(option -> option.getAttribute("value").getValue().equals(value)).count());

    }

}
