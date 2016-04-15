/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.duracloud.common.util.UserUtil;
import org.duracloud.storage.domain.DatabaseConfig;
import org.duracloud.storage.domain.DuraStoreInitConfig;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.duracloud.storage.provider.BrokeredStorageProvider;
import org.duracloud.storage.provider.StatelessStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: 5/27/11
 */
public class StorageProviderFactoryTest {

    private StorageAccountManager mockSAM;
    private StatelessStorageProvider mockSSP;
    private UserUtil mockUserUtil;

    private String acctId1 = "1";
    private String acctId2 = "2";

    private StorageAccount acct1;
    private StorageAccount acct2;

    private String acct1Name = "account-one";
    private String acct2Name = "account-two";

    private String instanceHost = "host";
    private String instancePort = "port";

    private List<String> storageAccountIds;
    private StorageProviderFactory factory;


    @Before
    public void startup() throws Exception {
        mockSAM = EasyMock.createMock(StorageAccountManager.class);
        mockSSP = EasyMock.createMock(StatelessStorageProvider.class);
        mockUserUtil = EasyMock.createMock(UserUtil.class);

        acct1 = new StorageAccountImpl(acctId1, "u", "p",
                                       StorageProviderType.AMAZON_S3);
        acct2 = new StorageAccountImpl(acctId2, "u", "p",
                                       StorageProviderType.RACKSPACE);

        storageAccountIds = new ArrayList<>();
        storageAccountIds.add(acctId1);
        storageAccountIds.add(acctId2);

        EasyMock.expect(mockSAM.isInitialized())
            .andReturn(true)
            .anyTimes();

        factory = new StorageProviderFactoryImpl(mockSAM, mockSSP, mockUserUtil);

    }

    private void replayMocks() {
        EasyMock.replay(mockSAM, mockSSP, mockUserUtil);
    }

    @After
    public void teardown() {
        EasyMock.verify(mockSAM, mockSSP, mockUserUtil);
    }

    @Test
    public void testGetStorageAccounts() {
        setUpMocksGetStorageAccounts();

        List<StorageAccount> accounts = factory.getStorageAccounts();
        assertNotNull(accounts);
        assertEquals(2, accounts.size());
    }

    private void setUpMocksGetStorageAccounts() {
        EasyMock.expect(mockSAM.getStorageAccountIds())
            .andReturn(storageAccountIds.iterator())
            .times(1);

        EasyMock.expect(mockSAM.getStorageAccount(EasyMock.isA(String.class)))
            .andReturn(acct1)
            .times(1);
        EasyMock.expect(mockSAM.getStorageAccount(EasyMock.isA(String.class)))
            .andReturn(acct2)
            .times(1);

        replayMocks();
    }

    @Test
    public void testGetStorageProvider() {
        setUpMocksGetStorageProvider();

        StorageProvider provider = factory.getStorageProvider();
        assertNotNull(provider);
        assertTrue(provider instanceof BrokeredStorageProvider);
        StorageProviderType type =
            ((BrokeredStorageProvider)provider).getTargetType();
        assertEquals(StorageProviderType.AMAZON_S3, type);
    }

    private void setUpMocksGetStorageProvider() {
        EasyMock.expect(mockSAM.getPrimaryStorageAccount())
            .andReturn(acct1)
            .times(2);
        EasyMock.expect(mockSAM.getStorageAccount(acctId1))
            .andReturn(null)
            .times(1);
        EasyMock.expect(mockSAM.getAccountName()).andReturn(acct1Name);

        replayMocks();
    }

    @Test
    public void testGetStorageProviderById() {
        setUpMocksGetStorageProviderById();

        StorageProvider provider = factory.getStorageProvider(acctId1);
        assertNotNull(provider);
        StorageProviderType type =
            ((BrokeredStorageProvider)provider).getTargetType();
        assertEquals(StorageProviderType.AMAZON_S3, type);
    }

    private void setUpMocksGetStorageProviderById() {
        EasyMock.expect(mockSAM.getStorageAccount(acctId1))
            .andReturn(acct1)
            .times(1);
        EasyMock.expect(mockSAM.getAccountName()).andReturn(acct1Name);

        replayMocks();
    }

    @Test
    public void testInitilize() throws Exception {
        //Test retrieving from accountManager
        StorageAccountManager sam = getProvider();

        //Test retrieving from cached providers
        StorageProvider provider = factory.getStorageProvider();
        assertNotNull(provider);
        assertTrue(provider instanceof BrokeredStorageProvider);
        StorageProviderType type =
            ((BrokeredStorageProvider)provider).getTargetType();
        assertEquals(StorageProviderType.AMAZON_S3, type);
        EasyMock.verify(sam);

        //reinitialize
        mockSAM.initialize(EasyMock.isA(List.class));
        EasyMock.expectLastCall();

        mockSAM.setEnvironment(instanceHost, instancePort, "account");
        EasyMock.expectLastCall();

        replayMocks();
        factory = new StorageProviderFactoryImpl(mockSAM, mockSSP, mockUserUtil);
        factory.initialize(createConfig(), instanceHost, instancePort, "account");

        //Test retrieving from accountManager now that the cache has been cleared
        getProvider();
    }

    protected DuraStoreInitConfig createConfig() {
        DuraStoreInitConfig config = new DuraStoreInitConfig();
        config.setStorageAccounts(Arrays.asList((StorageAccount) new StorageAccountImpl("id",
                                                                                        "username",
                                                                                        "password",
                                                                                        StorageProviderType.AMAZON_S3)));
        config.setMillDbConfig(new DatabaseConfig());
        return config;
    }


    @Test
    public void testInitilizeWithCachingEnabled() {
        //reinitialize
        mockSAM.initialize(EasyMock.isA(List.class));
        EasyMock.expectLastCall();

        EasyMock.expect(mockSAM.getStorageAccountIds())
        .andReturn(Arrays.asList(new String[]{acctId1}).iterator());

        EasyMock.expect(mockSAM.getStorageAccount(EasyMock.isA(String.class)))
            .andReturn(acct1);

        EasyMock.expect(mockSAM.getAccountName()).andReturn(acct1Name);

        mockSAM.setEnvironment(instanceHost, instancePort, "account");
        EasyMock.expectLastCall();

        replayMocks();

        
        factory = new StorageProviderFactoryImpl(mockSAM, mockSSP,
                                                 mockUserUtil, true);

        factory.initialize(createConfig(), instanceHost, instancePort,"account");
    }
    
    private StorageAccountManager getProvider() {
        StorageAccountManager sam =
            EasyMock.createMock(StorageAccountManager.class);

        EasyMock.expect(sam.isInitialized())
            .andReturn(true)
            .anyTimes();

        EasyMock.expect(sam.getPrimaryStorageAccount())
            .andReturn(acct1)
            .times(2);
        EasyMock.expect(sam.getStorageAccount(acctId1))
            .andReturn(acct1);
        EasyMock.expect(sam.getAccountName()).andReturn(acct1Name);

        EasyMock.replay(sam);

        factory = new StorageProviderFactoryImpl(sam, mockSSP, mockUserUtil);
        StorageProvider provider = factory.getStorageProvider();
        assertNotNull(provider);
        assertTrue(provider instanceof BrokeredStorageProvider);
        StorageProviderType type =
            ((BrokeredStorageProvider)provider).getTargetType();
        assertEquals(StorageProviderType.AMAZON_S3, type);

        return sam;
    }

}
