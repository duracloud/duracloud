/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.storage;

import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.storage.util.StorageProviderFactory;
import org.duracloud.durastore.util.StorageProviderFactoryImpl;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.provider.BrokeredStorageProvider;
import org.duracloud.storage.provider.StatelessStorageProviderImpl;
import org.duracloud.storage.provider.StorageProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Runtime test of Storage Provider utility classes.
 *
 * @author Bill Branan
 */
public class StorageProviderUtilsTest {

    private static String accountXml;

    @Before
    public void setUp() throws Exception {
        StringBuilder xml = new StringBuilder();
        xml.append("<storageProviderAccounts>");
        xml.append("  <storageAcct ownerId='0' isPrimary='1'>");
        xml.append("    <id>0</id>");
        xml.append("    <storageProviderType>AMAZON_S3</storageProviderType>");
        xml.append("    <storageProviderCredential>");
        EncryptionUtil encryptUtil = new EncryptionUtil();
        String username = encryptUtil.encrypt("username");
        xml.append("      <username>"+username+"</username>");
        String password = encryptUtil.encrypt("password");
        xml.append("      <password>"+password+"</password>");
        xml.append("    </storageProviderCredential>");
        xml.append("  </storageAcct>");
        xml.append("</storageProviderAccounts>");
        accountXml = xml.toString();
    }

    @Test
    public void testStorageAccountManager() throws Exception {
        InputStream is = new ByteArrayInputStream(accountXml.getBytes());
        StorageAccountManager acctManager = new StorageAccountManager();
        acctManager.initialize(is);
        assertNotNull(acctManager);

        StorageAccount primary = acctManager.getPrimaryStorageAccount();
        assertNotNull(primary);
        assertNotNull(primary.getUsername());
        assertEquals("username", primary.getUsername());
        assertNotNull(primary.getPassword());
        assertEquals("password", primary.getPassword());
        assertEquals(primary.getType(), StorageProviderType.AMAZON_S3);
    }

    @Test
    public void testStorageProviderUtility() throws Exception {
        InputStream is = new ByteArrayInputStream(accountXml.getBytes());
        StorageProviderFactory storageProviderFactory = new StorageProviderFactoryImpl(
            new StorageAccountManager(),
            new StatelessStorageProviderImpl());

        storageProviderFactory.initialize(is, "host", "port");
        StorageProvider storage =
            storageProviderFactory.getStorageProvider();

        assertNotNull(storage);
        assertTrue(storage instanceof BrokeredStorageProvider);
    }

}