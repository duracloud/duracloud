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
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.results.ServiceResultProcessor;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static org.duracloud.services.fixity.domain.FixityServiceOptions.HashApproach;
import static org.duracloud.services.fixity.domain.FixityServiceOptions.Mode;

/**
 * @author Andrew Woods
 *         Date: Aug 9, 2010
 */
public class FixityServiceTest {

    private FixityService fixity;
    private File workDir = new File("target/test-fixity-service");

    private String listingText;
    private final int NUM_WORK_ITEMS = 100;
    private final String listingSpaceId = "listing-space-id";
    private final String listingContentId = "listing-content-id";
    private final String outputSpaceId = "output-space-id";
    private final String outputContentId = "output-content-id";
    private final String spaceId = "space-id";
    private final String contentId = "content-id-";
    private final String hash = "hash-";

    @Before
    public void setUp() throws Exception {
        fixity = new FixityService();

        if (!workDir.exists()) {
            Assert.assertTrue(workDir.getCanonicalPath(), workDir.mkdir());
        }

        StringBuilder text = new StringBuilder("header,header,header");
        text.append(System.getProperty("line.separator"));
        for (int i = 0; i < NUM_WORK_ITEMS; ++i) {
            text.append(spaceId);
            text.append(",");
            text.append(contentId);
            text.append(i);
            text.append(",");
            text.append(hash);
            text.append(i);
            text.append(System.getProperty("line.separator"));
        }
        listingText = text.toString();

        setServiceOptions();
    }

    private void setServiceOptions() throws Exception {
        fixity.setMode(Mode.ALL_IN_ONE_LIST.getKey());
        fixity.setHashApproach(HashApproach.GENERATED.toString());
        fixity.setSalt("abc123");
        fixity.setFailFast(Boolean.TRUE.toString());
        fixity.setStoreId("1");
        fixity.setProvidedListingSpaceIdA(listingSpaceId);
        fixity.setProvidedListingContentIdA(listingContentId);
        fixity.setOutputSpaceId(outputSpaceId);
        fixity.setOutputContentId(outputContentId);
        fixity.setReportContentId("report-id");

        fixity.setThreads(3);
        fixity.setServiceWorkDir(workDir.getCanonicalPath());
        fixity.setContentStore(createMockContentStore());

    }

    @Test
    public void testStart() throws Exception {
        fixity.start();

        Map<String, String> props = null;
        boolean done = false;
        while (!done) {
            props = fixity.getServiceProps();
            Assert.assertNotNull(props);

            String status = props.get(ServiceResultProcessor.STATUS_KEY);
            Assert.assertNotNull(status);
            if (status.startsWith(ServiceResultListener.State.COMPLETE.name())) {
                done = true;
            }
        }

        verifyOutputFile();

        Assert.assertEquals(ComputeService.ServiceStatus.STARTED,
                            fixity.getServiceStatus());
    }

    private void verifyOutputFile() throws IOException {
        File outFile = new File(fixity.getServiceWorkDir(), outputContentId);
        Assert.assertTrue(outFile.exists());

        Map<String, String> idToMD5 = new HashMap<String, String>();

        BufferedReader reader = new BufferedReader(new FileReader(outFile));
        reader.readLine(); // not counting header line

        String line;
        int count = 0;
        while ((line = reader.readLine()) != null) {
            count++;

            String[] parts = line.split(",");
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
                new ByteArrayInputStream(new String("data" + i).getBytes())));
            Assert.assertEquals(expectedMd5, md5);
        }
    }

    @Test
    public void testStop() throws Exception {
        fixity.start();
        Assert.assertEquals(ComputeService.ServiceStatus.STARTED,
                            fixity.getServiceStatus());

        fixity.stop();
        Assert.assertEquals(ComputeService.ServiceStatus.STOPPED,
                            fixity.getServiceStatus());

        Map<String, String> props = fixity.getServiceProps();
        Assert.assertNotNull(props);
        String status = props.get(ServiceResultProcessor.STATUS_KEY);
        Assert.assertNotNull(status);
        Assert.assertTrue(status,
                          status.startsWith(ServiceResultListener.State.STOPPED.name()));
    }

    private ContentStore createMockContentStore() throws ContentStoreException {
        Content listingContent = createContent(listingText);
        ContentStore store = EasyMock.createMock("ContentStore",
                                                 ContentStore.class);

        EasyMock.expect(store.getContent(listingSpaceId, listingContentId))
            .andReturn(createContent(listingText));
        EasyMock.expect(store.getContent(listingSpaceId, listingContentId))
            .andReturn(createContent(listingText));
        for (int i = 0; i < NUM_WORK_ITEMS; ++i) {
            EasyMock.expect(store.getContent(spaceId, contentId + i)).andReturn(
                createContent("data" + i));
        }
        EasyMock.expect(store.addContent(EasyMock.eq(outputSpaceId),
                                         EasyMock.eq(outputContentId),
                                         EasyMock.<InputStream>anyObject(),
                                         EasyMock.anyLong(),
                                         EasyMock.eq("text/csv"),
                                         EasyMock.<String>isNull(),
                                         EasyMock.<Map<String, String>>isNull()))
            .andReturn("junk-md5")
            .anyTimes();

        EasyMock.makeThreadSafe(store, true);

        EasyMock.replay(store);
        return store;
    }

    private Content createContent(String text) {
        Content content = new Content();
        content.setStream(getContentStream(text));
        return content;
    }

    private InputStream getContentStream(String text) {
        return new AutoCloseInputStream(new ByteArrayInputStream(text.getBytes()));
    }

}
