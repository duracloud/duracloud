/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 * 
 * @author Daniel Bernstein
 *         Date: Jan 4, 2012
 */
public class DuracloudFileWriterTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreateWriteFlushClose() throws ContentStoreException, IOException {
        String spaceId = "spaceId";
        String contentId = "contentId";
        String mimetype = "plain/text";
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);
        EasyMock.expect(contentStore.getStoreId()).andReturn("storeId").once();
        EasyMock.expect(contentStore.addContent(EasyMock.isA(String.class),
                                EasyMock.isA(String.class),
                                EasyMock.isA(InputStream.class),
                                EasyMock.anyLong(),
                                EasyMock.isA(String.class),
                                (String)EasyMock.isNull(),
                                (Map<String,String>)EasyMock.isNull()))
                                .andReturn("test")
                                .times(2);
                    
        EasyMock.replay(contentStore);

        DuracloudFileWriter dfw =
            new DuracloudFileWriter(spaceId, contentId, mimetype, contentStore);        
        
        dfw.writeLine("Line 1");
        dfw.writeLine("Line 2");
        dfw.flush();
        
        dfw.close();

        try{
            dfw.writeLine("Line3");
            Assert.assertTrue(false);
        }catch(IOException ex){
            Assert.assertTrue(true);
        }

        try{
            dfw.flush();
            Assert.assertTrue(false);
        }catch(IOException ex){
            Assert.assertTrue(true);
        }

        try{
            dfw.close();
            Assert.assertTrue(false);
        }catch(IOException ex){
            Assert.assertTrue(true);
        }
 
        
        EasyMock.verify(contentStore);

    }

}
