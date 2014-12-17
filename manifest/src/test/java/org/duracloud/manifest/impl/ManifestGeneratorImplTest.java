/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import org.duracloud.common.constant.ManifestFormat;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestNotFoundException;
import org.duracloud.mill.db.model.ManifestItem;
import org.duracloud.mill.manifest.ManifestStore;
import org.duracloud.mill.test.AbstractTestBase;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.easymock.IAnswer;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ManifestGeneratorImplTest extends AbstractTestBase{

    private ManifestGeneratorImpl generator;
    private String account = "account";
    private String storeId = "store-id";
    private String spaceId = "space-id";

    @Mock
    private ManifestStore store;

    @Mock 
    private StorageProviderFactory storageProviderFactory;
    
    
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testBagit() throws Exception {
        testSuccessByFormat(ManifestFormat.BAGIT, false);
    }

    @Test
    public void testTsv() throws Exception {
        testSuccessByFormat(ManifestFormat.TSV, true);
    }

    protected void testSuccessByFormat(ManifestFormat format,
                                       boolean countHeader)
        throws ManifestArgumentException,
            ManifestNotFoundException,
            IOException {
        int count = 5;
        List<ManifestItem> list = new LinkedList<>();
        for(int i = 0; i < count; i++){
            ManifestItem item = createMockManifestItem();
            list.add(item);
        }
        expect(store.getItems(eq(account), eq(storeId), eq(spaceId)))
            .andReturn(list.iterator());
        
        mockStorageProviderFactory();
        
        replayAll();
        generator = new ManifestGeneratorImpl(store, storageProviderFactory);
        InputStream is = generator.getManifest(account,storeId, spaceId, format);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        int read = 0;
        while(true){
            if(reader.readLine() !=null){
                read++;
            }else{
                break;
            }
        }
        
        assertEquals(count+(countHeader? 1:0), read);
    }

    protected void mockStorageProviderFactory() {
        List<StorageAccount> storageAccounts = new LinkedList<>();
        expect(storageProviderFactory.getStorageAccounts()).andReturn(storageAccounts);
        StorageAccount storageAccount = createMock(StorageAccount.class);
        storageAccounts.add(storageAccount);
        expect(storageAccount.getId()).andReturn(storeId);
        StorageProvider store = createMock(StorageProvider.class);
        expect(storageProviderFactory.getStorageProvider(storeId)).andReturn(store);
        expect(store.getSpaceProperties(eq(spaceId))).andReturn(new HashMap<String,String>());
    }
    
    
    @Test
    public void testIteratorFailure() throws Exception {
        Iterator<ManifestItem> it = createMock(Iterator.class);

        expect(it.hasNext()).andReturn(true).times(3);
        expect(it.next()).andReturn(createMockManifestItem());
        expect(it.next()).andAnswer(new IAnswer<ManifestItem>() {
            @Override
            public ManifestItem answer() throws Throwable {
                throw new IOException("exception");
            }
        });
        
        expect(store.getItems(eq(account), eq(storeId), eq(spaceId))).andReturn(it);
        mockStorageProviderFactory();
        replayAll();
        generator = new ManifestGeneratorImpl(store, storageProviderFactory);
        InputStream is =
            generator.getManifest(account,storeId, spaceId, ManifestFormat.TSV);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        int read = 0;
        try {
            assertNotNull(reader.readLine());
            read++;
            assertNotNull(reader.readLine());
            read++;
            Thread.sleep(100);
            reader.readLine();
            fail("expected exception to be thrown before getting null from reader.readLine().");
        } catch (IOException e) {
            assertTrue(true);
        }
        
        assertEquals(2, read);
    }
    
    @Test
    public void testEmptyManifestTsv() throws Exception {
        testEmptyManifest(ManifestFormat.TSV);
    }
    
    @Test
    public void testEmptyManifestBagit() throws Exception {
        testEmptyManifest(ManifestFormat.BAGIT);
    }

    private void testEmptyManifest(ManifestFormat format) {
        Iterator<ManifestItem> it = createMock(Iterator.class);

        expect(it.hasNext()).andReturn(false);
                
        expect(store.getItems(eq(account), eq(storeId), eq(spaceId))).andReturn(it);
        mockStorageProviderFactory();
        
        replayAll();
        generator = new ManifestGeneratorImpl(store, storageProviderFactory);
        try {
            InputStream is = generator.getManifest(account,storeId, spaceId, format);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if(format.equals(ManifestFormat.TSV)){
                assertNotNull(reader.readLine());
            }
            assertEquals(0, is.available());
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    protected ManifestItem createMockManifestItem() {
        ManifestItem item = createMock(ManifestItem.class);
        expect(item.getContentChecksum()).andReturn("checksum");
        expect(item.getContentId()).andReturn("contentId");
        expect(item.getSpaceId()).andReturn(spaceId);
        return item;
    }
    

}
