/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.DateUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.aop.ContentMessage;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.duracloud.storage.aop.ContentMessage.ACTION.*;

/**
 * @author Andrew Woods
 *         Date: 3/21/12
 */
public class AuditLogStoreImplTest {

    private AuditLogStoreImpl logStore;

    private ContentStore contentStore;
    private static final String auditLogSpaceId = "x-admin";
    private static final String auditLogPrefixBase = "audit/audit-log-";
    private static final long auditLogSizeLimit = 100;

    private int idCounter = 0;

    @Before
    public void setUp() throws Exception {
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);

        File tmpDir = getTmpDir();
        String path = FilenameUtils.getPath(auditLogPrefixBase);
        FileUtils.deleteDirectory(new File(tmpDir, path));
    }

    private void replayMocks() {
        EasyMock.replay(contentStore);
    }

    @Test
    public void testWrite() throws Exception {
        String spaceId = "space-id";
        String auditLogPrefix = logPrefix("test0");

        List<String> contentIds = createLogContentIds(auditLogPrefix, spaceId);
        String newestContentId = contentIds.get(0);

        EasyMock.expect(contentStore.getSpaceContents(auditLogSpaceId,
                                                      auditLogPrefix + spaceId))
                .andReturn(contentIds.iterator());

        Content content = new Content();
        String text = "log text\n";
        content.setStream(asStream(text));
        EasyMock.expect(contentStore.getContent(auditLogSpaceId,
                                                newestContentId)).andReturn(
            content);


        List<ContentMessage> events = new ArrayList<ContentMessage>();
        events.add(createEvent(spaceId, INGEST));
        events.add(createEvent(spaceId, INGEST));
        events.add(createEvent(spaceId, COPY));

        long contentSize = getSize(events, text);
        expectAddContent(newestContentId, contentSize);

        replayMocks();

        logStore = new AuditLogStoreImpl(auditLogSpaceId,
                                         auditLogPrefix,
                                         auditLogSizeLimit);

        logStore.initialize(contentStore);
        logStore.write(events);
    }

    @Test
    public void testWriteNoneInExists() throws Exception {
        String spaceId = "space-id";
        String auditLogPrefix = logPrefix("test1");

        EasyMock.expect(contentStore.getSpaceContents(auditLogSpaceId,
                                                      auditLogPrefix + spaceId))
                .andReturn(new ArrayList<String>().iterator());

        List<ContentMessage> events = new ArrayList<ContentMessage>();
        events.add(createEvent(spaceId, INGEST));
        events.add(createEvent(spaceId, INGEST));
        events.add(createEvent(spaceId, COPY));

        String text = ContentMessage.tsvHeader() + "\n";
        long contentSize = getSize(events, text);
        expectAddContent(auditLogPrefix + ".*.tsv", contentSize);

        replayMocks();

        logStore = new AuditLogStoreImpl(auditLogSpaceId,
                                         auditLogPrefix,
                                         auditLogSizeLimit);

        logStore.initialize(contentStore);
        logStore.write(events);
    }

    @Test
    public void testWriteLocalExists() throws Exception {
        String spaceId = "space-id";
        String auditLogPrefix = logPrefix("test2");

        List<String> contentIds = createLogContentIds(auditLogPrefix, spaceId);
        String newestContentId = contentIds.get(0);
        String text = "some text\n";
        writeFiles(contentIds, text);

        List<ContentMessage> events = new ArrayList<ContentMessage>();
        events.add(createEvent(spaceId, INGEST));
        events.add(createEvent(spaceId, INGEST));
        events.add(createEvent(spaceId, COPY));

        long contentSize = getSize(events, text);
        expectAddContent(newestContentId, contentSize);

        replayMocks();

        logStore = new AuditLogStoreImpl(auditLogSpaceId,
                                         auditLogPrefix,
                                         auditLogSizeLimit);

        logStore.initialize(contentStore);
        logStore.write(events);
    }

    @Test
    public void testWriteEventSpacesMismatch() throws Exception {
        String spaceId = "space-id";
        String spaceIdMismatch = "space-id-mismatch";
        String auditLogPrefix = logPrefix("test3");

        List<ContentMessage> events = new ArrayList<ContentMessage>();
        events.add(createEvent(spaceId, INGEST));
        events.add(createEvent(spaceId, INGEST));
        events.add(createEvent(spaceId, COPY));
        ContentMessage event = createEvent(spaceIdMismatch, INGEST);
        events.add(event);

        replayMocks();

        logStore = new AuditLogStoreImpl(auditLogSpaceId,
                                         auditLogPrefix,
                                         auditLogSizeLimit);

        logStore.initialize(contentStore);

        try {
            logStore.write(events);
            Assert.fail("exception expected");

        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains(spaceIdMismatch));
        }
    }

    @Test
    public void testWriteLogRollover() throws Exception {
        String spaceId = "space-id";
        String auditLogPrefix = logPrefix("test4");

        List<String> contentIds = createLogContentIds(auditLogPrefix, spaceId);
        String newestContentId = contentIds.get(0);
        String text = "some text\n";
        writeFiles(contentIds, text);

        List<ContentMessage> events0 = new ArrayList<ContentMessage>();
        events0.add(createEvent(spaceId, INGEST));
        events0.add(createEvent(spaceId, INGEST));
        events0.add(createEvent(spaceId, COPY));

        List<ContentMessage> events1 = new ArrayList<ContentMessage>();
        events1.add(createEvent(spaceId, DELETE));
        events1.add(createEvent(spaceId, UPDATE));
        events1.add(createEvent(spaceId, UPDATE));

        long contentSize0 = getSize(events0, text);
        expectAddContent(newestContentId, contentSize0);

        long contentSize1 = getSize(events1, ContentMessage.tsvHeader() + "\n");
        expectAddContent(auditLogPrefix + ".*.tsv", contentSize1);

        replayMocks();

        logStore = new AuditLogStoreImpl(auditLogSpaceId,
                                         auditLogPrefix,
                                         auditLogSizeLimit);

        logStore.initialize(contentStore);
        logStore.write(events0);
        logStore.write(events1);
    }

    private void writeFiles(List<String> contentIds, String text)
        throws IOException {
        File tmpDir = getTmpDir();
        for (String contentId : contentIds) {
            FileUtils.writeStringToFile(new File(tmpDir, contentId), text);
        }
    }

    private File getTmpDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    private void expectAddContent(String contentIdPattern, long contentSize)
        throws ContentStoreException {
        EasyMock.expect(contentStore.addContent(EasyMock.eq(auditLogSpaceId),
                                                EasyMock.matches(
                                                    contentIdPattern),
                                                EasyMock.<InputStream>anyObject(),
                                                EasyMock.eq(contentSize),
                                                EasyMock.eq(
                                                    "text/tab-separated-values"),
                                                EasyMock.<String>notNull(),
                                                EasyMock.<Map<String, String>>isNull()))
                .andReturn("md5");
    }

    private List<String> createLogContentIds(String auditLogPrefix,
                                             String spaceId) {
        List<String> contentIds = new ArrayList<String>();

        long now = System.currentTimeMillis() - 50000;
        String time3 = DateUtil.convertToString(now);
        String time2 = DateUtil.convertToString(now - 10000);
        String time1 = DateUtil.convertToString(now - 20000);
        String time0 = DateUtil.convertToString(now - 30000);

        String contentId3 = auditLogPrefix + spaceId + "-" + time3 + ".tsv";
        String contentId2 = auditLogPrefix + spaceId + "-" + time2 + ".tsv";
        String contentId1 = auditLogPrefix + spaceId + "-" + time1 + ".tsv";
        String contentId0 = auditLogPrefix + spaceId + "-" + time0 + ".tsv";

        contentIds.add(contentId3);
        contentIds.add(contentId2);
        contentIds.add(contentId1);
        contentIds.add(contentId0);
        return contentIds;
    }

    private long getSize(List<ContentMessage> events, String text) {
        long size = text.length();
        for (ContentMessage event : events) {
            size += event.asTSV().length() + 1;
        }
        return size;
    }

    private InputStream asStream(String text) {
        return new AutoCloseInputStream(new ByteArrayInputStream(text.getBytes()));
    }

    private ContentMessage createEvent(String spaceId,
                                       ContentMessage.ACTION action) {
        ContentMessage msg = new ContentMessage();
        msg.setStoreId("store-id");
        msg.setSpaceId(spaceId);
        msg.setContentId("content-" + idCounter++);
        msg.setAction(action.name());
        return msg;
    }

    @Test
    public void testLogs() throws Exception {
        String spaceId = "space-id";
        String auditLogPrefix = logPrefix("test5");

        List<String> contentIds = createLogContentIds(auditLogPrefix, spaceId);

        Iterator<String> contentsItr = contentIds.iterator();
        EasyMock.expect(contentStore.getSpaceContents(auditLogSpaceId,
                                                      auditLogPrefix + spaceId))
                .andReturn(contentsItr);

        replayMocks();

        logStore = new AuditLogStoreImpl(auditLogSpaceId,
                                         auditLogPrefix,
                                         auditLogSizeLimit);

        logStore.initialize(contentStore);

        Iterator<String> logs = logStore.logs(spaceId);
        Assert.assertNotNull(logs);

        while (logs.hasNext()) {
            Assert.assertTrue(contentIds.remove(logs.next()));
        }
        Assert.assertEquals(0, contentIds.size());
    }

    @Test
    public void testRemove() throws Exception {
        String spaceId = "space-id";
        String auditLogPrefix = logPrefix("test6");

        List<String> contentIds = createLogContentIds(auditLogPrefix, spaceId);
        String text = ContentMessage.tsvHeader() + "\n";
        writeFiles(contentIds, text);

        for (String contentId : contentIds) {
            contentStore.deleteContent(auditLogSpaceId, contentId);
            EasyMock.expectLastCall();
        }

        replayMocks();

        logStore = new AuditLogStoreImpl(auditLogSpaceId,
                                         auditLogPrefix,
                                         auditLogSizeLimit);

        logStore.initialize(contentStore);

        for (String contentId : contentIds) {
            File logFile = new File(getTmpDir(), contentId);
            Assert.assertTrue(logFile.exists());

            logStore.removeLog(contentId);
            Assert.assertTrue(!logFile.exists());
        }
    }

    @Test
    public void testGetLogFile() throws Exception {
        String spaceId0 = "space-id";
        String spaceId1 = "space-id-another";

        String auditLogPrefix = logPrefix("test7");

        List<String> contentIds = createLogContentIds(auditLogPrefix, spaceId1);
        File expectedFile = new File(getTmpDir(), contentIds.get(0));
        String text = "text";
        writeFiles(contentIds, text);

        // for spaceId0 case (not found case)
        EasyMock.expect(contentStore.getSpaceContents(auditLogSpaceId,
                                                      auditLogPrefix +
                                                          spaceId0)).andReturn(
            new ArrayList<String>().iterator());

        replayMocks();

        logStore = new AuditLogStoreImpl(auditLogSpaceId,
                                         auditLogPrefix,
                                         auditLogSizeLimit);
        logStore.initialize(contentStore);

        File logFile0 = logStore.getLogFile(spaceId0);
        File logFile1 = logStore.getLogFile(spaceId1);

        verifyLogFile(expectedFile, logFile0, false);
        verifyLogFile(expectedFile, logFile1, true);
    }

    @Test
    public void testGetLogFileFromStore() throws Exception {
        String spaceId0 = "space-id";
        String spaceId1 = "space-id-another";

        String auditLogPrefix = logPrefix("test8");

        List<String> contentIds = createLogContentIds(auditLogPrefix, spaceId1);
        String newestContentId = contentIds.get(0);
        File expectedFile = new File(getTmpDir(), newestContentId);

        expectGetSpaceContents(spaceId0, auditLogPrefix, contentIds);
        expectGetSpaceContents(spaceId1, auditLogPrefix, contentIds);

        Content content = new Content();
        String text = "log text\n";
        content.setStream(asStream(text));
        EasyMock.expect(contentStore.getContent(auditLogSpaceId,
                                                newestContentId)).andReturn(
            content);

        replayMocks();

        logStore = new AuditLogStoreImpl(auditLogSpaceId,
                                         auditLogPrefix,
                                         auditLogSizeLimit);
        logStore.initialize(contentStore);

        File logFile0 = logStore.getLogFile(spaceId0);
        File logFile1 = logStore.getLogFile(spaceId1);

        verifyLogFile(expectedFile, logFile0, false);
        verifyLogFile(expectedFile, logFile1, true);
    }

    private void expectGetSpaceContents(String spaceId,
                                        String auditLogPrefix,
                                        List<String> contentIds)
        throws ContentStoreException {
        EasyMock.expect(contentStore.getSpaceContents(auditLogSpaceId,
                                                      auditLogPrefix + spaceId))
                .andReturn(contentIds.iterator());
    }

    private void verifyLogFile(File expected, File logFile, boolean valid) {
        Assert.assertNotNull(logFile);

        boolean matches =
            expected.getAbsolutePath().equals(logFile.getAbsolutePath());
        Assert.assertEquals(valid, matches);
    }

    private String logPrefix(String suffix) {
        return auditLogPrefixBase + suffix + "-";
    }
}
