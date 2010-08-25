/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.ComputeService;
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.results.ServiceResultProcessor;
import org.duracloud.services.fixity.util.StoreCaller;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.duracloud.services.fixity.results.ServiceResultListener.State.COMPLETE;

/**
 * @author Andrew Woods
 *         Date: Aug 11, 2010
 */
public class TestFixityService extends FixityServiceTestBase {

    private String compareReportGoodId = "output-compare-report-good-id";
    private String compareReportBadId = "output-compare-report-bad-id";

    private String allReportGoodId = "output-all-report-good-id";
    private String allReportBadId = "output-all-report-bad-id";

    private String adminSpaceId;

    private String targetSpaceId0;
    private String targetSpaceId1;
    private String outputGenSpaceId = "output-gen-space-id";
    private String outputGenListId = "output-gen-list-id";
    private String outputAllInOneGenSpaceId = "output-all-gen-space-id";
    private String outputAllInOneGenListId = "output-all-gen-list-id";
    private String salt = "abc123";


    @Before
    public void setUp() throws Exception {
        super.setUp();
        adminSpaceId = findSpaceId(adminSpacePrefix);
        targetSpaceId0 = findSpaceId(targetSpacePrefix0);
        targetSpaceId1 = findSpaceId(targetSpacePrefix1);
    }

    private void setUpForCompareMode(boolean isCorrupt) {
        fixity.setMode(FixityServiceOptions.Mode.COMPARE.getKey());
        fixity.setFailFast(Boolean.FALSE.toString());
        fixity.setStoreId("1");
        fixity.setProvidedListingSpaceIdA(adminSpaceId);
        fixity.setProvidedListingContentIdA(listingContentId);
        fixity.setProvidedListingSpaceIdB(adminSpaceId);
        fixity.setOutputSpaceId(adminSpaceId);
        if (isCorrupt) {
            fixity.setProvidedListingContentIdB(listingBadContentId);
            fixity.setReportContentId(compareReportBadId);
        } else {
            fixity.setProvidedListingContentIdB(listingGoodContentId);
            fixity.setReportContentId(compareReportGoodId);
        }
    }

    private void setUpForAllInOneSpaceMode() {
        fixity.setMode(FixityServiceOptions.Mode.ALL_IN_ONE_SPACE.getKey());
        fixity.setHashApproach(FixityServiceOptions.HashApproach.STORED.toString());
        fixity.setSalt("");
        fixity.setFailFast(Boolean.FALSE.toString());
        fixity.setStoreId("1");
        fixity.setProvidedListingSpaceIdA(adminSpaceId);
        fixity.setProvidedListingContentIdA(allContentId0);
        fixity.setTargetSpaceId(targetSpaceId0);
        fixity.setOutputSpaceId(adminSpaceId);
        fixity.setOutputContentId(outputAllInOneGenSpaceId);
        fixity.setReportContentId(allReportGoodId);
    }

    private void setUpForAllInOneListMode() {
        fixity.setMode(FixityServiceOptions.Mode.ALL_IN_ONE_LIST.getKey());
        fixity.setHashApproach(FixityServiceOptions.HashApproach.GENERATED.toString());
        fixity.setSalt("");
        fixity.setFailFast(Boolean.FALSE.toString());
        fixity.setStoreId("1");
        fixity.setProvidedListingSpaceIdA(adminSpaceId);
        fixity.setProvidedListingContentIdA(listingBadContentId);
        fixity.setOutputSpaceId(adminSpaceId);
        fixity.setOutputContentId(outputAllInOneGenListId);
        fixity.setReportContentId(allReportBadId);
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
        fixity.setMode(FixityServiceOptions.Mode.GENERATE_SPACE.getKey());
        fixity.setHashApproach(FixityServiceOptions.HashApproach.SALTED.toString());
        fixity.setSalt(salt);
        fixity.setStoreId("1");
        fixity.setTargetSpaceId(targetSpaceId0);
        fixity.setOutputSpaceId(adminSpaceId);
        fixity.setOutputContentId(outputGenSpaceId);
        fixity.setReportContentId("report-id");
    }

    @Test
    public void testStartCompareGoodMode() throws Exception {
        boolean isCorrupt = true;
        setUpForCompareMode(!isCorrupt);

        Map<ContentLocation, String> noCorruptItems = new HashMap<ContentLocation, String>();
        testStartCompare(compareReportGoodId, listingItems, noCorruptItems);
    }

    @Test
    public void testStartCompareBadMode() throws Exception {
        boolean isCorrupt = true;
        setUpForCompareMode(isCorrupt);

        testStartCompare(compareReportBadId,
                         listingItems,
                         corruptItemsToStatus);
    }

    @Test
    public void testStartAllInOneSpaceMode() throws Exception {
        setUpForAllInOneSpaceMode();

        Map<ContentLocation, String> noCorruptItems = new HashMap<ContentLocation, String>();
        testStartAllInOne(outputAllInOneGenSpaceId,
                          allReportGoodId,
                          allItems0,
                          noCorruptItems);
    }

    @Test
    public void testStartAllInOneListMode() throws Exception {
        setUpForAllInOneListMode();

        testStartAllInOne(outputAllInOneGenListId,
                          allReportBadId,
                          listingItems,
                          corruptItemsToStatus);
    }

    @Test
    public void testStartGenerateListMode() throws Exception {
        setUpForGenerateListMode();
        testStartGenerate(outputGenListId, listingItems, "");
    }

    @Test
    public void testStartGenerateSpaceMode() throws Exception {
        setUpForGenerateSpaceMode();

        // the service only runs over targetSpaceId0
        List<ContentLocation> trimmedItems = new ArrayList<ContentLocation>();
        for (ContentLocation item : itemToMd5.keySet()) {
            if (item.getSpaceId().startsWith(targetSpacePrefix0)) {
                trimmedItems.add(item);
            }
        }
        testStartGenerate(outputGenSpaceId, trimmedItems, salt);
    }

    private void testStartCompare(String contentId,
                                  List<ContentLocation> items,
                                  Map<ContentLocation, String> corruptItems)
        throws Exception {
        startAndWaitForCompletion(FixityService.PHASE_COMPARE);

        verifyCompareReportFile(contentId, items, corruptItems);
        verifyCompareReportContent(contentId, items, corruptItems);
    }

    private void testStartAllInOne(String hashContentId,
                                   String reportContentId,
                                   List<ContentLocation> items,
                                   Map<ContentLocation, String> corruptItems)
        throws Exception {
        startAndWaitForCompletion(FixityService.PHASE_COMPARE);

        verifyGeneratedHashFile(hashContentId, items, "");
        verifyGeneratedHashContent(hashContentId, items, "");

        verifyCompareReportFile(reportContentId, items, corruptItems);
        verifyCompareReportContent(reportContentId, items, corruptItems);
    }

    private void testStartGenerate(String contentId,
                                   Collection<ContentLocation> items,
                                   String salt) throws Exception {
        startAndWaitForCompletion(FixityService.PHASE_FIND);

        verifyGeneratedHashFile(contentId, items, salt);
        verifyGeneratedHashContent(contentId, items, salt);
    }

    private void startAndWaitForCompletion(String phase) throws Exception {
        fixity.start();
        waitForStateAndPhase(COMPLETE, phase);

        Assert.assertEquals(ComputeService.ServiceStatus.STARTED,
                            fixity.getServiceStatus());
    }

    private void waitForStateAndPhase(ServiceResultListener.State state,
                                      String phase) throws Exception {
        Map<String, String> props = null;
        ServiceResultListener.StatusMsg msg = null;
        boolean done = false;
        int MAX_TRIES = 120;
        int tries = 0;
        while (!done && tries < MAX_TRIES) {
            props = fixity.getServiceProps();
            Assert.assertNotNull(props);

            String status = props.get(ServiceResultProcessor.STATUS_KEY);
            if (status != null) {
                msg = new ServiceResultListener.StatusMsg(status);
                if (msg.getState().equals(state) &&
                    msg.getPhase().equals(phase)) {
                    done = true;
                }
            }
            tries++;
            sleep(500);
        }
        Assert.assertNotNull(msg);
        Assert.assertEquals(state, msg.getState());
        Assert.assertEquals(phase, msg.getPhase());
    }

    private void verifyCompareReportFile(String contentId,
                                         List<ContentLocation> items,
                                         Map<ContentLocation, String> corruptItems)
        throws IOException {
        BufferedReader reader = getFileReader(contentId);
        verifyCompareReport(reader, items, corruptItems);
    }

    private void verifyCompareReportContent(String contentId,
                                            List<ContentLocation> items,
                                            Map<ContentLocation, String> corruptItems)
        throws Exception {
        BufferedReader reader = getContentReader(contentId);
        verifyCompareReport(reader, items, corruptItems);
    }


    private void verifyCompareReport(BufferedReader reader,
                                     List<ContentLocation> items,
                                     Map<ContentLocation, String> corruptItems)
        throws IOException {

        Map<ContentLocation, String> itemToStatus = new HashMap<ContentLocation, String>();

        reader.readLine(); // not counting header line

        String line;
        int count = 0;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 0) {
                count++;

                String[] parts = line.split(",");
                Assert.assertEquals(5, parts.length);

                Assert.assertTrue(line,
                                  parts[0].startsWith(targetSpacePrefix0) ||
                                      parts[0].startsWith(targetSpacePrefix1));
                itemToStatus.put(new ContentLocation(parts[0], parts[1]),
                                 parts[4]);
            }
        }
        reader.close();

        Assert.assertEquals(items.size(), count);
        Assert.assertTrue(items.size() > 0);

        for (ContentLocation item : items) {
            String status = itemToStatus.get(item);
            Assert.assertNotNull(item.toString(), status);

            String corrupt = corruptItems.get(item);
            String expectedStatus = corrupt == null ? "VALID" : corrupt;
            Assert.assertEquals(expectedStatus, status);
        }
    }

    private void verifyGeneratedHashFile(String contentId,
                                         Collection<ContentLocation> items,
                                         String salt) throws IOException {
        BufferedReader reader = getFileReader(contentId);
        verifyGeneratedHashes(reader, items, salt);
    }

    private void verifyGeneratedHashContent(String contentId,
                                            Collection<ContentLocation> items,
                                            String salt) throws Exception {
        BufferedReader reader = getContentReader(contentId);
        verifyGeneratedHashes(reader, items, salt);
    }

    private void verifyGeneratedHashes(BufferedReader reader,
                                       Collection<ContentLocation> items,
                                       String salt) throws IOException {
        Map<ContentLocation, String> itemToMD5 = new HashMap<ContentLocation, String>();

        reader.readLine(); // not counting header line

        String line;
        int count = 0;
        while ((line = reader.readLine()) != null) {
            count++;

            String[] parts = line.split(",");
            Assert.assertEquals(3, parts.length);

            Assert.assertTrue(line,
                              parts[0].startsWith(targetSpacePrefix0) ||
                                  parts[0].startsWith(targetSpacePrefix1));
            itemToMD5.put(new ContentLocation(parts[0], parts[1]), parts[2]);
        }
        reader.close();

        Assert.assertEquals(items.size(), count);

        // uses knowledge of how content was created in "beforeClass()" above.
        Assert.assertTrue(items.size() > 0);
        for (ContentLocation item : items) {
            String md5 = itemToMD5.get(item);
            Assert.assertNotNull(md5);

            String expectedMd5 = getMd5("data-" + item.getContentId() + salt);
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
        waitForStateAndPhase(ServiceResultListener.State.STARTED,
                             FixityService.PHASE_FIND);
        Assert.assertEquals(ComputeService.ServiceStatus.STARTED,
                            fixity.getServiceStatus());

        fixity.stop();
        waitForStateAndPhase(ServiceResultListener.State.STOPPED,
                             FixityService.PHASE_FIND);
        Assert.assertEquals(ComputeService.ServiceStatus.STOPPED,
                            fixity.getServiceStatus());
    }

    private BufferedReader getFileReader(String contentId)
        throws FileNotFoundException {
        File outFile = new File(fixity.getServiceWorkDir(), contentId);
        Assert.assertTrue(outFile.exists());

        return new BufferedReader(new FileReader(outFile));
    }

    private BufferedReader getContentReader(String contentId) throws Exception {
        Content content = getContent(adminSpaceId, contentId);
        Assert.assertNotNull(content);

        return new BufferedReader(new InputStreamReader(content.getStream()));
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

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

}
