/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.duracloud.common.changenotifier.AccountChangeNotifier;
import org.duracloud.common.error.NoUserLoggedInException;
import org.duracloud.common.rest.DuraCloudRequestContextUtil;
import org.duracloud.common.util.UserUtil;
import org.duracloud.durastore.util.StorageProviderFactoryImpl;
import org.duracloud.storage.domain.DatabaseConfig;
import org.duracloud.storage.domain.DuraStoreInitConfig;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.duracloud.storage.provider.BrokeredStorageProvider;
import org.duracloud.storage.provider.StatelessStorageProviderImpl;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * Runtime test of Storage Provider utility classes.
 *
 * @author Bill Branan
 */
public class StorageProviderUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testStorageProviderUtilities() throws Exception {

        AccountChangeNotifier accountChangeNotifier = EasyMock.createMock(AccountChangeNotifier.class);
        StorageAccountManager acctManager = new StorageAccountManager();
        StorageProviderFactory storageProviderFactory =
            new StorageProviderFactoryImpl(acctManager,
                                           new StatelessStorageProviderImpl(),
                                           new TestUserUtil(),
                                           new DuraCloudRequestContextUtil(),
                                           accountChangeNotifier);

        DuraStoreInitConfig config = new DuraStoreInitConfig();
        config.setStorageAccounts(
            Arrays.asList((StorageAccount) new StorageAccountImpl("id", "username", "password",
                                                                  StorageProviderType.AMAZON_S3)));

        config.setMillDbConfig(new DatabaseConfig());

        EasyMock.replay(accountChangeNotifier);

        storageProviderFactory.initialize(config, "host", "port", "accountid");
        StorageProvider storage =
            storageProviderFactory.getStorageProvider();

        assertNotNull(storage);
        assertTrue(storage instanceof BrokeredStorageProvider);

        StorageAccount primary = acctManager.getPrimaryStorageAccount();
        assertNotNull(primary);
        assertNotNull(primary.getUsername());
        assertEquals("username", primary.getUsername());
        assertNotNull(primary.getPassword());
        assertEquals("password", primary.getPassword());
        assertEquals(primary.getType(), StorageProviderType.AMAZON_S3);
        EasyMock.verify(accountChangeNotifier);

    }

    private class TestUserUtil implements UserUtil {
        @Override
        public String getCurrentUsername() throws NoUserLoggedInException {
            return "user-name";
        }
    }

}