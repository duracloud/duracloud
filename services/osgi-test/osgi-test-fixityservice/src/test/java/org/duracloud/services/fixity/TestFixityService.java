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
import org.duracloud.services.fixity.util.StoreCaller;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.duracloud.common.util.ChecksumUtil.Algorithm.MD5;

/**
 * @author Andrew Woods
 *         Date: Aug 11, 2010
 */
public class TestFixityService extends FixityServiceTestBase {

    private String adminSpaceId;
    private String outputGenListId = "output-gen-list-id";

    private String targetSpaceId;
    private String outputGenSpaceId = "output-gen-space-id";
    private String salt = "abc123";


    @Before
    public void setUp() throws Exception {
        super.setUp();
        adminSpaceId = findSpaceId(adminSpacePrefix);
    }

    private void setUpForGenerateListMode() {
        fixity.setMode(FixityServiceOptions.Mode.GENERATE_LIST.getKey());
        fixity.setHashApproach(FixityServiceOptions.HashApproach.GENERATED.toString());
        fixity.setSalt("");
        fixity.setStoreId("1");
        fixity.setProvidedListingSpaceIdA(adminSpaceId);
        fixity.setProvidedListingContentIdA(listingContentId);
        fixity.setOutputSpaceId(adminSpaceId);
        fixity.setOutputContentId(outputGenListId);
        fixity.setReportContentId("report-id");
    }

    private void setUpForGenerateSpaceMode() {
        targetSpaceId = findSpaceId(targetSpacePrefix);

        fixity.setMode(FixityServiceOptions.Mode.GENERATE_SPACE.getKey());
        fixity.setHashApproach(FixityServiceOptions.HashApproach.SALTED.toString());
        fixity.setSalt(salt);
        fixity.setStoreId("1");
        fixity.setTargetSpaceId(targetSpaceId);
        fixity.setOutputSpaceId(adminSpaceId);
        fixity.setOutputContentId(outputGenSpaceId);
        fixity.setReportContentId("report-id");
    }

    @Test
    public void testStartGenerateListMode() throws Exception {
        setUpForGenerateListMode();
        testStart(outputGenListId, listingItems, "");
    }

    @Test
    public void testStartGenerateSpaceMode() throws Exception {
        setUpForGenerateSpaceMode();
        testStart(outputGenSpaceId, contentIdToMd5.keySet(), salt);
    }

    private void testStart(String contentId,
                           Collection<String> items,
                           String salt) throws Exception {
        fixity.start();

        Map<String, String> props = null;
        ServiceResultListener.StatusMsg msg;
        boolean done = false;
        while (!done) {
            props = fixity.getServiceProps();
            Assert.assertNotNull(props);

            String status = props.get(ServiceResultProcessor.STATUS_KEY);
            Assert.assertNotNull(status);

            msg = new ServiceResultListener.StatusMsg(status);
            if (ServiceResultListener.State.COMPLETE.equals(msg.getState())) {
                done = true;
            }
        }

        verifyGeneratedHashFile(contentId, items, salt);
        verifyGeneratedHashContent(contentId, items, salt);

        Assert.assertEquals(ComputeService.ServiceStatus.STARTED,
                            fixity.getServiceStatus());
    }

    private void verifyGeneratedHashFile(String contentId,
                                         Collection<String> items,
                                         String salt) throws IOException {
        File outFile = new File(fixity.getServiceWorkDir(), contentId);
        Assert.assertTrue(outFile.exists());

        BufferedReader reader = new BufferedReader(new FileReader(outFile));
        verifyGeneratedHashes(reader, items, salt);
    }

    private void verifyGeneratedHashContent(String contentId,
                                            Collection<String> items,
                                            String salt) throws Exception {
        Content content = getContent(adminSpaceId, contentId);
        Assert.assertNotNull(content);

        BufferedReader reader = new BufferedReader(new InputStreamReader(content.getStream()));
        verifyGeneratedHashes(reader, items, salt);
    }

    private void verifyGeneratedHashes(BufferedReader reader,
                                       Collection<String> items,
                                       String salt) throws IOException {
        Map<String, String> idToMD5 = new HashMap<String, String>();

        reader.readLine(); // not counting header line

        String line;
        int count = 0;
        String targetSpaceId = findSpaceId(targetSpacePrefix);
        while ((line = reader.readLine()) != null) {
            count++;

            String[] parts = line.split(",");
            Assert.assertEquals(3, parts.length);

            Assert.assertEquals(targetSpaceId, parts[0]);
            idToMD5.put(parts[1], parts[2]);
        }
        reader.close();
        Assert.assertEquals(items.size(), count);

        // uses knowledge of how content was created in "beforeClass()" above.
        Assert.assertTrue(items.size() > 0);
        for (String contentId : items) {
            String md5 = idToMD5.get(contentId);
            Assert.assertNotNull(md5);

            String expectedMd5 = getMd5("data-" + contentId + salt);
            Assert.assertEquals(expectedMd5, md5);
        }
    }

    @Test
    public void testStopGenerateListMode() throws Exception {
        setUpForGenerateListMode();
        testStop();
    }

    @Test
    public void testStopGenerateSpaceMode() throws Exception {
        setUpForGenerateSpaceMode();
        testStop();
    }

    private void testStop() throws Exception {
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

        ServiceResultListener.StatusMsg msg = new ServiceResultListener.StatusMsg(
            status);
        Assert.assertEquals(ServiceResultListener.State.STOPPED,
                            msg.getState());
    }


    private Content getContent(final String spaceId, final String contentId)
        throws Exception {
        final ContentStore store = super.createContentStore();
        StoreCaller<Content> caller = new StoreCaller<Content>() {
            @Override
            protected Content doCall() throws ContentStoreException {
                Content content = null;
                int tries = 0;
                while (null == content && tries++ < 4) {
                    content = store.getContent(spaceId, contentId);
                }
                Assert.assertNotNull(spaceId + "/" + contentId, content);
                return content;
            }

            @Override
            public String getLogMessage() {
                StringBuilder sb = new StringBuilder("Content item not found: ");
                sb.append(spaceId);
                sb.append("/");
                sb.append(contentId);
                return sb.toString();
            }
        };
        return caller.call();
    }

}
