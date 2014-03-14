/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.domain.Content;
import org.duracloud.storage.aop.ContentMessage;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.duracloud.common.util.DateUtil.convertToStringVerbose;
import static org.duracloud.manifest.ManifestGenerator.FORMAT;
import static org.duracloud.storage.aop.ContentMessage.ACTION.COPY;
import static org.duracloud.storage.aop.ContentMessage.ACTION.DELETE;
import static org.duracloud.storage.aop.ContentMessage.ACTION.INGEST;

/**
 * @author Andrew Woods
 *         Date: 3/29/12
 */
public class ManifestGeneratorImplTest {

    private ManifestGeneratorImpl generator;

    private ContentStoreManager storeManager;
    private final String auditLogSpace = "audit-log-space";

    private ContentStore primaryStore;

    private final String storeId = "store-id";
    private final String spaceId = "space-id";
    private final FORMAT format = FORMAT.BAGIT;

    private static final int NUM_LOGS = 2;
    private List<File> tmpFiles;

    @Before
    public void setUp() throws Exception {
        tmpFiles = new ArrayList<File>();

        storeManager = EasyMock.createMock("ContentStoreManager",
                                           ContentStoreManager.class);
        primaryStore = EasyMock.createMock("PrimaryStore",
                                           ContentStore.class);

        EasyMock.expect(storeManager.getPrimaryContentStore()).andReturn(
            primaryStore);
        EasyMock.expect(primaryStore.getStoreId()).andReturn(storeId);

        generator = new ManifestGeneratorImpl(auditLogSpace,
                                              new FileCleaningTracker());
    }

    @After
    public void tearDown() throws Exception {
        for (File file : tmpFiles) {
            FileUtils.deleteQuietly(file);
        }

        EasyMock.verify(storeManager,  primaryStore);
    }

    private void replayMocks() {
        EasyMock.replay(storeManager, primaryStore);

        generator.initialize(storeManager);
    }

    // FIXME This tests are currently broken - manifest must be rewritten to use
    // new auditor.

    /*
    @Test
    public void testGetManifest() throws Exception {
        doTestGetManifest(MODE.MODE_INGEST);
    }

    @Test
    public void testGetManifestWithDeletes() throws Exception {
        doTestGetManifest(MODE.MODE_DELETE);
    }

    @Test
    public void testGetManifestWithDates() throws Exception {
        doTestGetManifest(MODE.MODE_DATE);
    }

    @Test
    public void testGetManifestWithStoreIds() throws Exception {
        doTestGetManifest(MODE.MODE_STOREID);
    }

    @Test
    public void testGetManifestWithStoreIdsNull() throws Exception {
        doTestGetManifest(MODE.MODE_STOREID_NULL);
    }
    */

    private void doTestGetManifest(MODE mode) throws Exception {

        List<String> logs = new ArrayList<String>();
        for (int i = 0; i < NUM_LOGS; ++i) {
            EasyMock.expect(storeManager.getPrimaryContentStore()).andReturn(
                primaryStore);

            String logName = logName(i);
            logs.add(logName);
            createLog(i, mode);

            Content content = new Content();
            content.setStream(getLogStream(i));
            EasyMock.expect(primaryStore.getContent(auditLogSpace, logName))
                    .andReturn(content);
        }


        replayMocks();

        String cStoreId = storeId;
        if (mode.equals(MODE.MODE_STOREID_NULL)) {
            cStoreId = null;
        }

        InputStream stream = generator.getManifest(cStoreId,
                                                   spaceId,
                                                   format,
                                                   getDate());
        Assert.assertNotNull(stream);

        List<String> expectedIds = getExpectedContentIds(mode);
        verifyStream(stream, expectedIds);
    }

    private void verifyStream(InputStream stream, List<String> expectedIds)
        throws IOException {
        List<String> lines = IOUtils.readLines(stream);
        Assert.assertEquals(expectedIds.size(), lines.size());

        for (String expectedId : expectedIds) {
            Iterator<String> lineItr = lines.iterator();
            boolean found = false;
            while (lineItr.hasNext()) {
                String line = lineItr.next();
                if (line.contains(spaceId + "/" + expectedId) &&
                    line.contains(getContentMd5(expectedId))) {
                    lineItr.remove();
                    found = true;
                }
            }
            Assert.assertTrue("contentId not found: " + expectedId, found);
        }

        Assert.assertEquals(0, lines.size());
    }

    private InputStream getLogStream(int i) throws IOException {
        return FileUtils.openInputStream(tmpFiles.get(i));
    }

    private String logName(int i) {
        return "audit-log-" + i;
    }

    private void createLog(int i, MODE mode) throws IOException {
        List<ContentMessage> events = getEvents(i, mode);
        File log = getTempFile(i);
        OutputStream output = FileUtils.openOutputStream(log);

        IOUtils.write(ContentMessage.tsvHeader() + "\n", output);
        for (ContentMessage event : events) {
            IOUtils.write(event.asTSV() + "\n", output);
        }

        IOUtils.closeQuietly(output);
    }

    private File getTempFile(int i) throws IOException {
        File file = File.createTempFile("manifest-gen", "-log-" + i);
        tmpFiles.add(file);
        return file;
    }

    private List<ContentMessage> getEvents(int i, MODE mode) {
        List<ContentMessage> events = new ArrayList<ContentMessage>();

        switch (mode) {
            case MODE_INGEST:
                int day = 0;
                if (0 == i) {
                    events.add(createEvent(INGEST, 0, day));
                    events.add(createEvent(INGEST, 1, day));
                    events.add(createEvent(INGEST, 2, day));
                    events.add(createEvent(INGEST, 3, day));
                    events.add(createEvent(INGEST, 4, day));

                } else if (1 == i) {
                    events.add(createEvent(INGEST, 0, day));
                    events.add(createEvent(INGEST, 1, day));
                    events.add(createEvent(INGEST, 2, day));
                    events.add(createEvent(INGEST, 3, day));
                    events.add(createEvent(INGEST, 4, day));

                } else {
                    Assert.fail("unexpected index: " + i);
                }
                break;

            case MODE_DELETE:
                day = 0;
                if (0 == i) {
                    events.add(createEvent(INGEST, 0, day));
                    events.add(createEvent(COPY, 1, day));
                    events.add(createEvent(INGEST, 2, day));
                    events.add(createEvent(DELETE, 3, day));
                    events.add(createEvent(INGEST, 4, day));

                } else if (1 == i) {
                    events.add(createEvent(DELETE, 0, day));
                    events.add(createEvent(INGEST, 1, day));
                    events.add(createEvent(INGEST, 2, day));
                    events.add(createEvent(INGEST, 3, day));
                    events.add(createEvent(DELETE, 4, day));

                } else {
                    Assert.fail("unexpected index: " + i);
                }
                break;

            case MODE_DATE:
                int dayBefore = -1;
                day = 0;
                int dayAfter = 1;
                if (0 == i) {
                    events.add(createEvent(INGEST, 0, dayBefore));
                    events.add(createEvent(INGEST, 1, day));
                    events.add(createEvent(INGEST, 2, dayBefore));
                    events.add(createEvent(INGEST, 3, dayAfter));
                    events.add(createEvent(INGEST, 4, dayAfter));

                } else if (1 == i) {
                    events.add(createEvent(COPY, 2, dayAfter));
                    events.add(createEvent(INGEST, 3, dayAfter));
                    events.add(createEvent(COPY, 4, dayAfter));

                } else {
                    Assert.fail("unexpected index: " + i);
                }
                break;

            case MODE_STOREID:
            case MODE_STOREID_NULL:
                day = 0;
                String storeIdSuffix0 = "";
                String storeIdSuffix1 = "1";
                if (0 == i) {
                    events.add(createEvent(INGEST, 0, day, storeIdSuffix0));
                    events.add(createEvent(INGEST, 1, day, storeIdSuffix0));
                    events.add(createEvent(INGEST, 2, day, storeIdSuffix0));
                    events.add(createEvent(INGEST, 3, day, storeIdSuffix0));
                    events.add(createEvent(INGEST, 4, day, storeIdSuffix0));

                } else if (1 == i) {
                    events.add(createEvent(DELETE, 0, day, storeIdSuffix0)); //
                    events.add(createEvent(DELETE, 1, day, storeIdSuffix1));
                    events.add(createEvent(DELETE, 2, day, storeIdSuffix1));
                    events.add(createEvent(DELETE, 3, day, storeIdSuffix1));
                    events.add(createEvent(DELETE, 4, day, storeIdSuffix1));

                } else {
                    Assert.fail("unexpected index: " + i);
                }
                break;
        }

        return events;
    }

    private List<String> getExpectedContentIds(MODE mode) {
        List<String> contentIds = new ArrayList<String>();

        switch (mode) {
            case MODE_INGEST:
                contentIds.add(getContentId(0));
                contentIds.add(getContentId(1));
                contentIds.add(getContentId(2));
                contentIds.add(getContentId(3));
                contentIds.add(getContentId(4));
                break;

            case MODE_DELETE:
                contentIds.add(getContentId(1));
                contentIds.add(getContentId(2));
                contentIds.add(getContentId(3));
                break;

            case MODE_DATE:
                contentIds.add(getContentId(0));
                contentIds.add(getContentId(1));
                contentIds.add(getContentId(2));
                break;

            case MODE_STOREID:
            case MODE_STOREID_NULL:
                contentIds.add(getContentId(1));
                contentIds.add(getContentId(2));
                contentIds.add(getContentId(3));
                contentIds.add(getContentId(4));
                break;
        }
        return contentIds;
    }

    private ContentMessage createEvent(ContentMessage.ACTION action,
                                       int i,
                                       int day) {
        return createEvent(action, i, day, "");
    }

    private ContentMessage createEvent(ContentMessage.ACTION action,
                                       int i,
                                       int day,
                                       String storeIdSuffix) {
        String contentId = getContentId(i);

        ContentMessage event = new ContentMessage();
        event.setAction(action.name());
        event.setStoreId(storeId + storeIdSuffix);
        event.setSpaceId(spaceId);
        event.setContentId(contentId);
        event.setContentMd5(getContentMd5(contentId));
        event.setDatetime(convertToStringVerbose(getDate(day).getTime()));

        return event;
    }

    private Date getDate() {
        return getDate(0);
    }

    private Date getDate(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, day);
        return calendar.getTime();
    }

    private String getContentMd5(String contentId) {
        return "md5" + contentId;
    }

    private String getContentId(int i) {
        return "contentId-" + i;
    }

    protected static enum MODE {
        MODE_INGEST, MODE_DELETE, MODE_DATE, MODE_STOREID, MODE_STOREID_NULL
    }

//    @Test
//    public void testGetManifestEmptySpace() throws Exception {
//
//        EasyMock.expect(storeManager.getPrimaryContentStore())
//                .andReturn(primaryStore);
//
//        EasyMock.expect(primaryStore.getSpaceProperties(spaceId))
//                .andReturn(new HashMap<String, String>());
//
//        replayMocks();
//
//        InputStream stream = generator.getManifest(null,
//                                                   spaceId,
//                                                   FORMAT.TSV,
//                                                   getDate());
//        Assert.assertNotNull(stream);
//        List<String> lines = IOUtils.readLines(stream);
//        Assert.assertEquals(1, lines.size());
//    }
//
//    @Test
//    public void testGetManifestEmptySpaceSecondaryStore() throws Exception {
//
//        EasyMock.expect(storeManager.getContentStore(storeId))
//                .andReturn(primaryStore);
//
//        EasyMock.expect(primaryStore.getSpaceProperties(spaceId))
//                .andReturn(new HashMap<String, String>());
//
//        replayMocks();
//
//        InputStream stream = generator.getManifest(storeId,
//                                                   spaceId,
//                                                   FORMAT.BAGIT,
//                                                   getDate());
//        Assert.assertNotNull(stream);
//        List<String> lines = IOUtils.readLines(stream);
//        Assert.assertEquals(0, lines.size());
//    }

}
