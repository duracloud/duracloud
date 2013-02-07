/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.ComputeService;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.results.ServiceResultProcessor;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.duracloud.services.ComputeService.DELIM;
import static org.duracloud.services.fixity.domain.FixityServiceOptions.Mode;
import static org.duracloud.services.fixity.results.ServiceResultListener.State.COMPLETE;

/**
 * @author Andrew Woods
 *         Date: Aug 9, 2010
 */
public class FixityServiceTest {

    private FixityService fixity;
    private ContentStore store;
    private File workDir = new File("target/test-fixity-service");

    private String listingText;
    private String corruptListingText;
    private final int NUM_WORK_ITEMS = 100;

    private final String salt = "abc123";
    private final String spaceIdA = "space-id-a";
    private final String contentIdA = "content-id-a";
    private final String spaceIdB = "space-id-b";
    private final String contentIdB = "content-id-b";
    private final String targetSpaceId = "target-space-id";
    private final String outputSpaceId = "output-space-id";
    private final String outputContentId = "output-content-id";
    private final String reportContentId = "report-content-id";

    private final String spaceId = "space-id";
    private final String contentId = "content-id-";
    private final String hash = "hash-";

    @Before
    public void setUp() throws Exception {
        fixity = new FixityService();

        store = EasyMock.createMock("ContentStore",
                                    ContentStore.class);

        if (!workDir.exists()) {
            Assert.assertTrue(workDir.getCanonicalPath(), workDir.mkdir());
        }

        boolean isCorrupt = true;
        listingText = createListing(!isCorrupt);
        corruptListingText = createListing(isCorrupt);
    }

    @After
    public void tearDown() {
        EasyMock.verify(store);
    }

    private String createListing(boolean isCorrupt) {
        StringBuilder text = new StringBuilder(
            "header" + DELIM + "header" + DELIM + "header");
        text.append(System.getProperty("line.separator"));

        for (int i = 0; i < NUM_WORK_ITEMS; ++i) {
            if (isCorrupt &&
                (i == 2 || i == 22)) { // leave off the 2nd and 22nd items
                continue;
            }
            text.append(spaceId);
            text.append(DELIM);
            text.append(contentId);
            text.append(i);
            text.append(DELIM);
            text.append(hash);
            text.append(i);

            if (isCorrupt && (i % 8 == 0)) { // corrupt every 8th item
                text.append("-junk");
            }
            text.append(System.getProperty("line.separator"));
        }
        return text.toString();
    }

    private void setServiceOptions(Mode mode) throws Exception {
        fixity.setHashApproach(FixityServiceOptions.HashApproach.SALTED.name());
        fixity.setSalt(salt);
        fixity.setFailFast(Boolean.TRUE.toString());
        fixity.setStoreId("1");
        fixity.setProvidedListingSpaceIdA(spaceIdA);
        fixity.setProvidedListingContentIdA(contentIdA);
        fixity.setProvidedListingSpaceIdB(spaceIdB);
        fixity.setProvidedListingContentIdB(contentIdB);
        fixity.setTargetSpaceId(targetSpaceId);
        fixity.setOutputSpaceId(outputSpaceId);
        fixity.setOutputContentId(outputContentId);
        fixity.setReportContentId(reportContentId);

        fixity.setMode(mode.getKey());
        switch (mode) {
            case ALL_IN_ONE_LIST:
            case ALL_IN_ONE_SPACE:
                fixity.setProvidedListingSpaceIdB(null);
                fixity.setProvidedListingContentIdB(null);
                fixity.setTargetSpaceId(null);
                break;

            case GENERATE_LIST:
                fixity.setFailFast(null);
                fixity.setProvidedListingSpaceIdB(null);
                fixity.setProvidedListingContentIdB(null);
                fixity.setTargetSpaceId(null);
                break;

            case GENERATE_SPACE:
                fixity.setFailFast(null);
                fixity.setProvidedListingSpaceIdA(null);
                fixity.setProvidedListingContentIdA(null);
                fixity.setProvidedListingSpaceIdB(null);
                fixity.setProvidedListingContentIdB(null);
                break;

            case COMPARE:
                fixity.setHashApproach(null);
                fixity.setSalt(null);
                fixity.setTargetSpaceId(null);
                fixity.setOutputContentId(null);
                break;

            default:
                Assert.fail("Unexpected Mode: " + mode);
        }

        fixity.setThreads(3);
        fixity.setServiceWorkDir(workDir.getCanonicalPath());
        fixity.setContentStore(createMockContentStore(mode));
    }

    @Test
    public void testStartCompareMode() throws Exception {
        setServiceOptions(Mode.COMPARE);

        fixity.start();

        boolean isValid = true;
        waitForState(COMPLETE);
        verifyOutputReportFile(isValid);

        Assert.assertEquals(ComputeService.ServiceStatus.SUCCESS,
                            fixity.getServiceStatus());
    }

    @Test
    public void testStartAllInOneListMode() throws Exception {
        setServiceOptions(Mode.ALL_IN_ONE_LIST);

        fixity.start();

        waitForState(COMPLETE);

        boolean isValid = true;
        verifyOutputHashFile();
        verifyOutputReportFile(!isValid);

        Assert.assertEquals(ComputeService.ServiceStatus.SUCCESS,
                            fixity.getServiceStatus());
    }

    @Test
    public void testStartGenerateListMode() throws Exception {
        setServiceOptions(Mode.GENERATE_LIST);

        fixity.start();

        waitForState(COMPLETE);

        verifyOutputHashFile();

        Assert.assertEquals(ComputeService.ServiceStatus.SUCCESS,
                            fixity.getServiceStatus());
    }

    private void waitForState(ServiceResultListener.State state) {
        Map<String, String> props = null;
        ServiceResultProcessor.StatusMsg msg;
        boolean done = false;
        int MAX_TRIES = 60;
        int tries = 0;
        while (!done && tries < MAX_TRIES) {
            props = fixity.getServiceProps();
            Assert.assertNotNull(props);

            String status = props.get(ServiceResultProcessor.STATUS_KEY);
            if (status != null) {
                msg = new ServiceResultListener.StatusMsg(status);
                if (msg.getState().equals(state)) {
                    done = true;
                }
            }
            tries++;
            sleep(500);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    private void verifyOutputHashFile() throws IOException {
        File outFile = new File(fixity.getServiceWorkDir(), outputContentId);
        Assert.assertTrue(outFile.exists());

        Map<String, String> idToMD5 = new HashMap<String, String>();

        BufferedReader reader = new BufferedReader(new FileReader(outFile));
        reader.readLine(); // not counting header line

        String line;
        int count = 0;
        while ((line = reader.readLine()) != null) {
            count++;

            String[] parts = line.split(String.valueOf(DELIM));
            Assert.assertEquals(3, parts.length);

            Assert.assertEquals(spaceId, parts[0]);
            idToMD5.put(parts[1], parts[2]);
        }
        reader.close();
        Assert.assertEquals(NUM_WORK_ITEMS, count);

        // uses knowledge of how content was created in "createMockContentStore()" below.
        ChecksumUtil checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        for (int i = 0; i < NUM_WORK_ITEMS; ++i) {
            String md5 = idToMD5.get(contentId + i);
            Assert.assertNotNull(md5);

            String expectedMd5 = checksumUtil.generateChecksum(new AutoCloseInputStream(
                new ByteArrayInputStream(new String(
                    "data" + i + salt).getBytes())));
            Assert.assertEquals(expectedMd5, md5);
        }
    }

    private void verifyOutputReportFile(boolean success) throws Exception {
        File outFile = new File(fixity.getServiceWorkDir(), reportContentId);
        Assert.assertTrue(outFile.exists());

        Map<String, String> idToStatus = new HashMap<String, String>();

        BufferedReader reader = new BufferedReader(new FileReader(outFile));
        reader.readLine(); // not counting header line

        String line;
        int count = 0;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 0) {
                count++;

                String[] parts = line.split(String.valueOf(DELIM));
                Assert.assertEquals(line, 5, parts.length);

                Assert.assertEquals(spaceId, parts[0]);
                Assert.assertTrue(parts[1], parts[1].startsWith(contentId));
                idToStatus.put(parts[1], parts[4]);
            }
        }
        reader.close();
        Assert.assertEquals(NUM_WORK_ITEMS, count);

        // uses knowledge of how corrupt content was created in "setUp()" above.
        String valid = "VALID";
        String mismatch = "MISMATCH";
        String missing = "MISSING_FROM_1";
        for (int i = 0; i < NUM_WORK_ITEMS; ++i) {
            String status = idToStatus.get(contentId + i);
            Assert.assertNotNull(status);

            if (i == 2 || i == 22) {
                Assert.assertEquals(success ? valid : missing, status);
            } else if (i % 8 == 0) {
                Assert.assertEquals(success ? valid : mismatch, status);
            } else {
                Assert.assertEquals(valid, status);
            }
        }
    }

    @Test
    public void testStop() throws Exception {
        setServiceOptions(Mode.GENERATE_SPACE);

        fixity.start();

        sleep(1000); // do some work        

        ComputeService.ServiceStatus status = fixity.getServiceStatus();
        Assert.assertEquals(ComputeService.ServiceStatus.SUCCESS, status);

        sleep(1000); // do some work

        fixity.stop();
        Assert.assertEquals(ComputeService.ServiceStatus.STOPPED,
                            fixity.getServiceStatus());

        Map<String, String> props = fixity.getServiceProps();
        Assert.assertNotNull(props);
        String statusMsg = props.get(ServiceResultProcessor.STATUS_KEY);
        Assert.assertNotNull(statusMsg);
        Assert.assertNotNull(props.get(ComputeService.ITEMS_PROCESS_COUNT));
        Assert.assertNotNull(props.get(ComputeService.PASS_COUNT_KEY));
        Assert.assertNotNull(props.get(ComputeService.FAILURE_COUNT_KEY));

        ServiceResultListener.StatusMsg msg = new ServiceResultListener.StatusMsg(
            statusMsg);
        Assert.assertEquals(ServiceResultListener.State.STOPPED,
                            msg.getState());
    }

    private ContentStore createMockContentStore(Mode mode)
        throws ContentStoreException {
        switch (mode) {
            case GENERATE_SPACE: // used by testStop
                EasyMock.expect(store.getSpaceContents(targetSpaceId))
                    .andReturn(newIterator(19750))
                    .anyTimes();
                EasyMock.expect(store.getContent(targetSpaceId, "")).andReturn(
                    createContent("nothing")).anyTimes();
                break;

            case GENERATE_LIST:
                // need to specify two getContent(spaceA,contentA) for counting thread
                EasyMock.expect(store.getContent(spaceIdA, contentIdA))
                    .andReturn(createContent(listingText));
                EasyMock.expect(store.getContent(spaceIdA, contentIdA))
                    .andReturn(createContent(listingText));
                break;

            case ALL_IN_ONE_SPACE:
            case ALL_IN_ONE_LIST:
                // need to specify two getContent(spaceA,contentA) for counting thread
                EasyMock.expect(store.getContent(spaceIdA, contentIdA))
                    .andReturn(createContent(listingText));
                EasyMock.expect(store.getContent(spaceIdA, contentIdA))
                    .andReturn(createContent(listingText));
                EasyMock.expect(store.getContent(spaceIdA, contentIdA))
                    .andReturn(createContent(listingText));
                EasyMock.expect(store.getContent(outputSpaceId,
                                                 outputContentId)).andReturn(
                    createContent(corruptListingText)).anyTimes();
                store.deleteContent(EasyMock.eq(outputSpaceId),
                                    EasyMock.eq(outputContentId));
                EasyMock.expectLastCall();
                break;

            case COMPARE:
                EasyMock.expect(store.getContent(spaceIdA, contentIdA))
                    .andReturn(createContent(listingText));
                EasyMock.expect(store.getContent(spaceIdB, contentIdB))
                    .andReturn(createContent(listingText));
                break;

        }
        EasyMock.expect(store.addContent(EasyMock.eq(outputSpaceId),
                                         EasyMock.eq(reportContentId),
                                         EasyMock.<InputStream>anyObject(),
                                         EasyMock.gt(0L),
                                         EasyMock.eq("text/tab-separated-values"),
                                         EasyMock.<String>isNull(),
                                         EasyMock.<Map<String, String>>isNull()))
            .andReturn("")
            .anyTimes();

        for (int i = 0; i < NUM_WORK_ITEMS; ++i) {
            EasyMock.expect(store.getContent(spaceId, contentId + i)).andReturn(
                createContent("data" + i)).anyTimes();
        }
        EasyMock.expect(store.addContent(EasyMock.eq(outputSpaceId),
                                         EasyMock.eq(outputContentId),
                                         EasyMock.<InputStream>anyObject(),
                                         EasyMock.anyLong(),
                                         EasyMock.eq("text/tab-separated-values"),
                                         EasyMock.<String>isNull(),
                                         EasyMock.<Map<String, String>>isNull()))
            .andReturn("junk-md5")
            .anyTimes();
        EasyMock.expect(store.getSpaceProperties(outputSpaceId)).andReturn(null);

        EasyMock.makeThreadSafe(store, true);

        EasyMock.replay(store);
        return store;
    }

    private Iterator<String> newIterator(int count) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < count; ++i) {
            list.add("");
        }
        return list.iterator();
    }

    private Content createContent(String text) {
        Content content = new Content();
        content.setStream(getContentStream(text));
        return content;
    }

    private InputStream getContentStream(String text) {
        return new AutoCloseInputStream(new ByteArrayInputStream(text.getBytes()));
    }

    @Test
    public void testGetHashingHeader() {
        String header = fixity.getHashingHeader();
        Assert.assertEquals("space-id" + DELIM + "content-id" + DELIM + "MD5",
                            header);

        EasyMock.replay(store);
    }

    @Test
    public void testGetComparingHeader() {
        String header = fixity.getComparingHeader(spaceIdA,
                                                  contentIdA,
                                                  spaceIdB,
                                                  contentIdB);
        String expected = "space-id" + DELIM + "content-id" + DELIM + "0:" +
                          spaceIdA + "/" + contentIdA + DELIM + "1:" +
                          spaceIdB + "/" + contentIdB + DELIM + "status";
        Assert.assertEquals(expected, header);

        EasyMock.replay(store);
    }

}
