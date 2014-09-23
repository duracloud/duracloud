/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.reader.impl;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;

import org.duracloud.audit.AuditConfig;
import org.duracloud.audit.reader.AuditLogEmptyException;
import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.mill.test.AbstractTestBase;
import org.junit.Before;
import org.junit.Test;
/**
 * 
 * @author Daniel Bernstein
 *         Date: Sept. 17, 2014
 *
 */
public class AuditLogReaderImplTest extends AbstractTestBase {

    private String spaceId  = "space-id";
    private String storeId = "store-id";
    private String account = "account";
    private String globalAuditSpaceId = "global-audit-space-id";


    @Before
    public void setUp() throws Exception {
    }


    @Test
    public void testGetAuditLog() throws IOException, ContentStoreException, AuditLogEmptyException {
        
        String[]  file1Lines = {"a", "b", "c", "d"};
        String[]  file2Lines = {"e", "f", "g", "h"};

        String prefix = getPrefix();
        final ContentStore contentStore = createMock(ContentStore.class);
        String contentId1 = "log1";
        String contentId2 = "log2";
        Iterator<String> it =
            Arrays.asList(new String[] { prefix + "/" + contentId1,
                                        prefix + "/" + contentId2 }).iterator();
        expect(contentStore.getSpaceContents(eq(globalAuditSpaceId), eq(prefix))).andReturn(it);
        AuditConfig config = createMock(AuditConfig.class);
        expect(config.getSpaceId()).andReturn(globalAuditSpaceId );
        
        setupGetContentCall(prefix, contentStore, contentId1, file1Lines);
        setupGetContentCall(prefix, contentStore, contentId2, file2Lines);

        replayAll();

        AuditLogReaderImpl auditReader = createAuditLogReader(contentStore, config);

        InputStream is = auditReader.gitAuditLog(account, storeId, spaceId);
        
        assertNotNull(is);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        
        String line = reader.readLine();
        
        
        assertEquals(file1Lines[0], line);
        
        int count = 1;
        int totalCount = 1;
        String[] lines = file1Lines;
        while(true){
            line = reader.readLine();
            assertEquals(lines[count], line);
            count++;
            totalCount++;
            
            if(totalCount >= file1Lines.length + file2Lines.length-1){
                break;
            }else if(lines == file1Lines && count >= file1Lines.length){
                count = 1;
                lines = file2Lines;
            }
        }
        
        assertNull(reader.readLine());
        
        assertEquals(file1Lines.length+file2Lines.length-1, totalCount);
    }


    
    @Test
    public void testGetEmptyLog() throws IOException, ContentStoreException {
        replayAll();
        
        String prefix = getPrefix();
        final ContentStore contentStore = createMock(ContentStore.class);
        Iterator<String> it =
            Arrays.asList(new String[] { }).iterator();
        expect(contentStore.getSpaceContents(eq(globalAuditSpaceId), eq(prefix))).andReturn(it);
        AuditConfig config = createMock(AuditConfig.class);
        expect(config.getSpaceId()).andReturn(globalAuditSpaceId );
        
        replayAll();
        AuditLogReaderImpl auditReader =
            createAuditLogReader(contentStore, config);

        
        try{
            auditReader.gitAuditLog(account, storeId, spaceId);
            fail("expected to fail with empty log exception");
        }catch(AuditLogEmptyException e){}
    }

    
    @Test
    public void testContentFailure() throws IOException, ContentStoreException {
        replayAll();
        
        String[]  file1Lines = {"a","b"};

        String prefix = getPrefix();
        final ContentStore contentStore = createMock(ContentStore.class);
        String contentId1 = "log1";
        String contentId2 = "log2";

        Iterator<String> it =
            Arrays.asList(new String[] { prefix + "/" + contentId1,  prefix + "/" + contentId2,
            }).iterator();
        expect(contentStore.getSpaceContents(eq(globalAuditSpaceId), eq(prefix))).andReturn(it);
        AuditConfig config = createMock(AuditConfig.class);
        expect(config.getSpaceId()).andReturn(globalAuditSpaceId );
        
        setupGetContentCall(prefix, contentStore, contentId1, file1Lines);
        setupGetContentCallFailure(prefix, contentStore, contentId2, null);

        replayAll();

        AuditLogReaderImpl auditReader = createAuditLogReader(contentStore, config);

        try {
            InputStream is = auditReader.gitAuditLog(account, storeId, spaceId);
            sleep();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            assertNotNull(reader.readLine());
            assertNotNull(reader.readLine());
            reader.readLine();
            fail("expected reader exception");
        }catch( AuditLogEmptyException ex){
            fail("did not expect empty log exception");
        }catch(IOException ex){
            //all good.
        }
        
    }


    protected String getPrefix() {
        String prefix = account + "/" + storeId+"/"+spaceId +"/";
        return prefix;
    }


    protected void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setupGetContentCallFailure(String prefix,
                                            ContentStore contentStore,
                                            String contentId,
                                            String[] file1Lines) throws IOException, ContentStoreException {
        InputStream is = createMock(InputStream.class);
        expect(is.read(isA(byte[].class), anyInt(), anyInt())).andThrow(new IOException("test"));
        Content content = createMock(Content.class);
        expect(content.getStream()).andReturn(is);
        expect(contentStore.getContent(eq(globalAuditSpaceId),eq(prefix + "/" + contentId))).andReturn(content);
    }


    protected AuditLogReaderImpl
        createAuditLogReader(final ContentStore contentStore, AuditConfig config) {
        AuditLogReaderImpl auditReader = new AuditLogReaderImpl(config){
            @Override
            protected ContentStore getContentStore(AuditConfig auditConfig) {
                return contentStore;
            }
        };
        return auditReader;
    }
    
    protected void setupGetContentCall(String prefix, final ContentStore contentStore,
                                       String contentId,
                                       String[] fileLines)
        throws IOException,
            FileNotFoundException,
            ContentStoreException {
        File file = File.createTempFile(contentId, "txt");
        file.deleteOnExit();
        FileWriter writer = new FileWriter(file);
        for(String line : fileLines){
            writer.write(line+"\n");
        }
        writer.close();
        
        Content content = createMock(Content.class);
        expect(content.getStream()).andReturn(new FileInputStream(file));
        expect(contentStore.getContent(eq(globalAuditSpaceId),eq(prefix + "/" + contentId))).andReturn(content);
    }

}
