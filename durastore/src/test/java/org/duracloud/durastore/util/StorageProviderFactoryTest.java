/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.duracloud.storage.provider.BrokeredStorageProvider;
import org.duracloud.storage.provider.StatelessStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: 5/27/11
 */
public class StorageProviderFactoryTest {

    private StorageAccountManager mockSAM;
    private StatelessStorageProvider mockSSP;

    private String acctId1 = "1";
    private String acctId2 = "2";

    private StorageAccount acct1;
    private StorageAccount acct2;

    private List<String> storageAccountIds;
    private StorageProviderFactory factory;

    @Before
    public void startup() {
        mockSAM = EasyMock.createMock(StorageAccountManager.class);
        mockSSP = EasyMock.createMock(StatelessStorageProvider.class);

        acct1 = new StorageAccountImpl(acctId1,
                                       "u",
                                       "p",
                                       StorageProviderType.AMAZON_S3);
        acct2 = new StorageAccountImpl(acctId2, "u", "p",
                                       StorageProviderType.RACKSPACE);

        storageAccountIds = new ArrayList<String>();
        storageAccountIds.add(acctId1);
        storageAccountIds.add(acctId2);

        EasyMock.expect(mockSAM.isInitialized())
            .andReturn(true)
            .anyTimes();

        factory = new StorageProviderFactoryImpl(mockSAM, mockSSP);
    }

    @After
    public void teardown() {
        EasyMock.verify(mockSAM, mockSSP);
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

        EasyMock.replay(mockSAM, mockSSP);
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
        EasyMock.expect(mockSAM.getStorageAccount(
            StorageProviderFactoryImpl.PRIMARY))
            .andReturn(null)
            .times(1);
        EasyMock.expect(mockSAM.getPrimaryStorageAccount())
            .andReturn(acct1)
            .times(1);

        EasyMock.replay(mockSAM, mockSSP);
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

        EasyMock.replay(mockSAM, mockSSP);
    }

    @Test
    public void testInitilize() {
        //Test retrieving from accountManager
        getProvider();

        //Test retrieving from cached providers
        StorageProvider provider = factory.getStorageProvider();
        assertNotNull(provider);
        assertTrue(provider instanceof BrokeredStorageProvider);
        StorageProviderType type =
            ((BrokeredStorageProvider)provider).getTargetType();
        assertEquals(StorageProviderType.AMAZON_S3, type);

        //reinitialize
        mockSAM.initialize(EasyMock.isA(InputStream.class));
        EasyMock.expectLastCall();

        InputStream inputStream = EasyMock.createMock("InputSream",
                                                      InputStream.class);
        EasyMock.replay(mockSAM, mockSSP, inputStream);
        factory = new StorageProviderFactoryImpl(mockSAM, mockSSP);
        factory.initialize(inputStream);

        //Test retrieving from accountManager now that the cache has been cleared
        getProvider();
    }

    private void getProvider() {
        StorageAccountManager sam = EasyMock.createMock(StorageAccountManager.class);

        EasyMock.expect(sam.isInitialized())
            .andReturn(true)
            .anyTimes();

        EasyMock.expect(sam.getStorageAccount(StorageProviderFactoryImpl.PRIMARY))
            .andReturn(acct1)
            .times(1);

        EasyMock.replay(sam);

        factory = new StorageProviderFactoryImpl(sam, mockSSP);
        StorageProvider provider = factory.getStorageProvider();
        assertNotNull(provider);
        assertTrue(provider instanceof BrokeredStorageProvider);
        StorageProviderType type =
            ((BrokeredStorageProvider)provider).getTargetType();
        assertEquals(StorageProviderType.AMAZON_S3, type);

        EasyMock.verify(sam);
    }

}
