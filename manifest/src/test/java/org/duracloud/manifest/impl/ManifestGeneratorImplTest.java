package org.duracloud.manifest.impl;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.duracloud.manifest.ManifestGenerator.FORMAT;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestEmptyException;
import org.duracloud.mill.db.model.ManifestItem;
import org.duracloud.mill.manifest.ManifestStore;
import org.duracloud.mill.test.AbstractTestBase;
import org.easymock.IAnswer;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;

public class ManifestGeneratorImplTest extends AbstractTestBase{

    private ManifestGeneratorImpl generator;
    private String storeId = "store-id";
    private String spaceId = "space-id";

    @Mock
    private ManifestStore store;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testBagit() throws Exception {
        testSuccessByFormat(FORMAT.BAGIT, false);
    }

    @Test
    public void testTsv() throws Exception {
        testSuccessByFormat(FORMAT.TSV, true);
    }

    protected void testSuccessByFormat(FORMAT format, boolean countHeader)
        throws ManifestArgumentException,
            ManifestEmptyException,
            IOException {
        int count = 5;
        List<ManifestItem> list = new LinkedList<>();
        for(int i = 0; i < count; i++){
            ManifestItem item = createMockManifestItem();
            
            list.add(item);
        }
        expect(store.getItems(eq(storeId), eq(spaceId))).andReturn(list.iterator());
        replayAll();
        generator = new ManifestGeneratorImpl(store);
        InputStream is = generator.getManifest(storeId, spaceId, format);
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
        
        expect(store.getItems(eq(storeId), eq(spaceId))).andReturn(it);
        replayAll();
        generator = new ManifestGeneratorImpl(store);
        InputStream is = generator.getManifest(storeId, spaceId, FORMAT.TSV);
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
    public void testEmptyManifest() throws Exception {
        Iterator<ManifestItem> it = createMock(Iterator.class);

        expect(it.hasNext()).andReturn(false);
                
        expect(store.getItems(eq(storeId), eq(spaceId))).andReturn(it);
        replayAll();
        generator = new ManifestGeneratorImpl(store);
        try {
            InputStream is = generator.getManifest(storeId, spaceId, FORMAT.TSV);
            fail("expected manifest empty exception");
        } catch (ManifestEmptyException e) {
            assertTrue(true);
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
