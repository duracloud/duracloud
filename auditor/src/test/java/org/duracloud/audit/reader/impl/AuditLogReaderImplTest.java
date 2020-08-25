/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.reader.impl;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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

import org.duracloud.audit.AuditLogUtil;
import org.duracloud.audit.reader.AuditLogReaderException;
import org.duracloud.error.ContentStoreException;
import org.duracloud.mill.test.AbstractTestBase;
import org.duracloud.storage.domain.AuditConfig;
import org.duracloud.storage.domain.RetrievedContent;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Daniel Bernstein
 * Date: Sept. 17, 2014
 */
public class AuditLogReaderImplTest extends AbstractTestBase {

    private String spaceId = "space-id";
    private String storeId = "store-id";
    private String account = "account";
    private String globalAuditSpaceId = "global-audit-space-id";

    @Mock
    private StorageProvider storageProvider;

    @Mock
    private AuditConfig config;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetAuditLog() throws IOException, ContentStoreException {

        String[] file1Lines = {"a", "b", "c", "d"};
        String[] file2Lines = {"e", "f", "g", "h"};

        String prefix = getPrefix();
        final StorageProvider storageProvider = createMock(StorageProvider.class);
        String contentId1 = "log1";
        String contentId2 = "log2";
        Iterator<String> it =
            Arrays.asList(new String[] {prefix + "/" + contentId1,
                                        prefix + "/" + contentId2}).iterator();
        expect(storageProvider.getSpaceContents(eq(globalAuditSpaceId), eq(prefix))).andReturn(it);
        AuditConfig config = createMock(AuditConfig.class);
        mockCheckEnabled(config);
        expect(config.getAuditLogSpaceId()).andReturn(globalAuditSpaceId);

        setupGetContentCall(prefix, storageProvider, contentId1, file1Lines);
        setupGetContentCall(prefix, storageProvider, contentId2, file2Lines);

        replayAll();

        AuditLogReaderImpl auditReader = createAuditLogReader(storageProvider, config);

        InputStream is = auditReader.getAuditLog(account, storeId, spaceId);

        assertNotNull(is);

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String line = reader.readLine();

        assertEquals(file1Lines[0], line);

        int count = 1;
        int totalCount = 1;
        String[] lines = file1Lines;
        while (true) {
            line = reader.readLine();
            assertEquals(lines[count], line);
            count++;
            totalCount++;

            if (totalCount >= file1Lines.length + file2Lines.length - 1) {
                break;
            } else if (lines == file1Lines && count >= file1Lines.length) {
                count = 1;
                lines = file2Lines;
            }
        }

        assertNull(reader.readLine());

        assertEquals(file1Lines.length + file2Lines.length - 1, totalCount);
    }

    @Test
    public void testGetLogNotFound() throws IOException, StorageException {

        String prefix = getPrefix();
        expect(storageProvider.getSpaceContents(eq(globalAuditSpaceId), eq(prefix)))
            .andThrow(new NotFoundException("not found"));

        expect(config.getAuditLogSpaceId()).andReturn(globalAuditSpaceId);
        mockCheckEnabled(config);

        replayAll();
        AuditLogReaderImpl auditReader =
            createAuditLogReader(storageProvider, config);

        try {
            auditReader.getAuditLog(account, storeId, spaceId);
            fail("expected to fail with empty log exception");
        } catch (AuditLogReaderException e) {
            // Expected exception
        }
    }

    @Test
    public void testEmptyLog() throws IOException, StorageException {

        String prefix = getPrefix();
        Iterator<String> it =
            Arrays.asList(new String[] {}).iterator();
        expect(storageProvider.getSpaceContents(eq(globalAuditSpaceId), eq(prefix))).andReturn(it);

        expect(config.getAuditLogSpaceId()).andReturn(globalAuditSpaceId);

        mockCheckEnabled(config);
        replayAll();
        AuditLogReaderImpl auditReader =
            createAuditLogReader(storageProvider, config);

        try {
            InputStream is = auditReader.getAuditLog(account, storeId, spaceId);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();
            assertEquals(AuditLogUtil.getHeader(), line);
            assertEquals(-1, buf.read());
        } catch (Exception e) {
            fail("unexpected to fail with empty log exception");
        }
    }

    private void mockCheckEnabled(AuditConfig config) {
        expect(config.getAuditLogSpaceId()).andReturn(globalAuditSpaceId);
        expect(config.getAuditQueueName()).andReturn("queue");
    }

    @Test
    public void testContentFailure() throws IOException, ContentStoreException {
        String[] file1Lines = {"a", "b"};

        String prefix = getPrefix();
        String contentId1 = "log1";
        String contentId2 = "log2";

        Iterator<String> it =
            Arrays.asList(new String[] {prefix + "/" + contentId1, prefix + "/" + contentId2,}).iterator();
        expect(storageProvider.getSpaceContents(eq(globalAuditSpaceId), eq(prefix))).andReturn(it);
        expect(config.getAuditLogSpaceId()).andReturn(globalAuditSpaceId);

        setupGetContentCall(prefix, storageProvider, contentId1, file1Lines);
        setupGetContentCallFailure(prefix, storageProvider, contentId2, null);
        mockCheckEnabled(config);

        replayAll();

        AuditLogReaderImpl auditReader = createAuditLogReader(storageProvider, config);

        try {
            InputStream is = auditReader.getAuditLog(account, storeId, spaceId);
            sleep();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            assertNotNull(reader.readLine());
            assertNotNull(reader.readLine());
            reader.readLine();
            fail("expected reader exception");
        } catch (AuditLogReaderException ex) {
            fail("did not expect empty log exception");
        } catch (IOException ex) {
            //all good.
        }

    }

    protected String getPrefix() {
        String prefix = account + "/" + storeId + "/" + spaceId + "/";
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
                                            StorageProvider storageProvider,
                                            String contentId,
                                            String[] file1Lines) throws IOException, ContentStoreException {
        InputStream is = createMock(InputStream.class);
        RetrievedContent retrievedContent = new RetrievedContent();
        retrievedContent.setContentStream(is);

        expect(is.read(isA(byte[].class), anyInt(), anyInt())).andThrow(new IOException("test"));
        expect(storageProvider.getContent(eq(globalAuditSpaceId), eq(prefix + "/" + contentId)))
            .andReturn(retrievedContent);

        is.close();
        expectLastCall();
    }

    protected AuditLogReaderImpl createAuditLogReader(final StorageProvider storageProvider, AuditConfig config) {
        AuditLogReaderImpl auditReader = new AuditLogReaderImpl() {
            @Override
            protected StorageProvider getStorageProvider() {
                return storageProvider;
            }
        };

        auditReader.initialize(config);

        return auditReader;
    }

    protected void setupGetContentCall(String prefix, final StorageProvider storageProvider,
                                       String contentId,
                                       String[] fileLines)
        throws IOException, FileNotFoundException, ContentStoreException {
        File file = File.createTempFile(contentId, "txt");
        file.deleteOnExit();
        FileWriter writer = new FileWriter(file);
        for (String line : fileLines) {
            writer.write(line + "\n");
        }
        writer.close();

        RetrievedContent retrievedContent = new RetrievedContent();
        retrievedContent.setContentStream(new FileInputStream(file));
        expect(storageProvider.getContent(eq(globalAuditSpaceId), eq(prefix + "/" + contentId)))
            .andReturn(retrievedContent);
    }

}
