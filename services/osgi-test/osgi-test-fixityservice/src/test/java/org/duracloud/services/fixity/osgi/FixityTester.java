/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.osgi;

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.AclType;
import org.duracloud.common.model.RootUserCredential;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.ComputeService;
import org.duracloud.services.fixity.FixityService;
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.results.ServiceResultProcessor;
import org.duracloud.services.fixity.util.StoreCaller;
import org.duracloud.servicesutil.util.DuraConfigAdmin;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.duracloud.common.util.ChecksumUtil.Algorithm.MD5;

/**
 * @author Andrew Woods
 *         Date: Aug 16, 2010
 */
public class FixityTester {

    private FixityService fixity;
    private DuraConfigAdmin configAdmin;
    private File workDir;
    private String version;
    private String port;

    private static final ChecksumUtil checksumUtil = new ChecksumUtil(MD5);
    private static final String adminSpacePrefix = "test-fixity-osgi-admin-";
    private static final String targetSpacePrefix = "test-fixity-osgi-target-";
    private static final String targetContentPrefix = "test/content/id-";
    private static final String outputGenContentId = "output-gen-items.csv";
    private static final String outputAllInOneContentId = "output-all-items.csv";
    private static final String outputReportId = "output-report.csv";

    private static final int NUM_ITEMS = 25;

    private final String targetSpaceId = targetSpacePrefix + randomInt();
    private final String adminSpaceId = adminSpacePrefix + randomInt();
    private List<ContentLocation> items = new ArrayList<ContentLocation>();


    private static final String storeIdS3 = "1";

    private ContentStore contentStore;

    public FixityTester(FixityService fixityService,
                        DuraConfigAdmin configAdmin,
                        File workDir,
                        String port,
                        String version) throws ContentStoreException {
        Assert.assertNotNull(fixityService);
        Assert.assertNotNull(configAdmin);
        Assert.assertNotNull(workDir);
        Assert.assertNotNull(port);
        Assert.assertNotNull(version);

        this.fixity = fixityService;
        this.configAdmin = configAdmin;
        this.workDir = workDir;
        this.port = port;
        this.version = version;

        this.contentStore = createContentStore(port);
        Assert.assertNotNull(contentStore);
    }

    public void testFixity() {

        try {
            doTest();

        } catch (Exception e) {
        } finally {
            try {
                fixity.stop();
            } catch (Exception e) {
            }
            tearDown();
        }
    }

    private void doTest() throws Exception {
        setUp();

        // Generate initial md5 listing over test space
        configureForGenerateMode();
        waitForConfigMode(FixityServiceOptions.Mode.GENERATE_SPACE.getKey());

        fixity.start();
        waitForCompletion(FixityService.PHASE_FIND);

        verifyGeneratedHashContent(outputGenContentId);

        // Re-generate listing using previous as standard of comparison
        configureForAllInOneMode();
        waitForConfigMode(FixityServiceOptions.Mode.ALL_IN_ONE_LIST.getKey());

        fixity.start();
        waitForCompletion(FixityService.PHASE_COMPARE);

        verifyReport(outputReportId);
    }

    private void setUp() throws IOException {
        if (!workDir.exists()) {
            Assert.assertTrue(workDir.getCanonicalPath(), workDir.mkdir());
        }
        fixity.setServiceWorkDir(workDir.getCanonicalPath());

        createSpace(adminSpaceId);
        createSpace(targetSpaceId);

        // create content
        ContentLocation loc;
        for (int i = 0; i < NUM_ITEMS; ++i) {
            loc = new ContentLocation(targetSpaceId, targetContentPrefix + i);
            this.items.add(loc);
            addContent(loc, "data-" + loc.getContentId());
        }

        verifySpace(adminSpaceId);
        verifySpace(targetSpaceId);
        verifyContent(this.items);
    }

    private void createSpace(String spaceId) {
        boolean success = false;
        int tries = 0;
        final int MAX_TRIES = 4;
        while (!success && tries < MAX_TRIES) {
            try {
                contentStore.createSpace(spaceId);
                success = true;
            } catch (ContentStoreException e) {
                sleep(1000);
                tries++;
            }
        }
    }

    private ContentStore createContentStore(String port)
        throws ContentStoreException {
        ContentStoreManager contentStoreManager = new ContentStoreManagerImpl(
            "localhost",
            port);
        Assert.assertNotNull(contentStoreManager);
        contentStoreManager.login(getRoot());

        return contentStoreManager.getContentStore(storeIdS3);
    }

    private RootUserCredential getRoot() {
        return new RootUserCredential();
    }

    private String addContent(final ContentLocation loc, final String text) {
        ContentStoreException cse = null;
        StringBuilder err = null;

        String mime = "text/plain";
        long contentSize = text.length();
        Map<String, String> properties = null;

        int MAX_TRIES = 4;
        int tries = 0;
        String md5 = null;
        while (null == md5 && tries < MAX_TRIES) {
            InputStream content = new AutoCloseInputStream(new ByteArrayInputStream(
                text.getBytes()));

            try {
                md5 = contentStore.addContent(loc.getSpaceId(),
                                              loc.getContentId(),
                                              content,
                                              contentSize,
                                              mime,
                                              null,
                                              properties);
            } catch (ContentStoreException e) {
                tries++;
                cse = e;
                IOUtils.closeQuietly(content);

                err = new StringBuilder("Error adding content: ");
                err.append(loc.getSpaceId());
                err.append("/");
                err.append(loc.getContentId());
                System.err.println(err.toString());
            }
        }

        Assert.assertNotNull(err.toString() + ", " + cse.getMessage(), md5);
        return md5;
    }

    private void configureForGenerateMode() throws Exception {
        Map<String, String> props = new HashMap<String, String>();

        props.put("mode", FixityServiceOptions.Mode.GENERATE_SPACE.getKey());
        props.put("hashApproach",
                  FixityServiceOptions.HashApproach.GENERATED.toString());
        props.put("salt", "");
        props.put("storeId", storeIdS3);
        props.put("targetSpaceId", targetSpaceId);
        props.put("outputSpaceId", adminSpaceId);
        props.put("outputContentId", outputGenContentId);
        props.put("reportContentId", "report-id");

        props.put("username", getRoot().getUsername());
        props.put("password", getRoot().getPassword());
        props.put("duraStorePort", port);

        configAdmin.updateConfiguration(getConfigId(), props);
    }


    private void configureForAllInOneMode() throws Exception {
        Map<String, String> props = new HashMap<String, String>();

        props.put("mode", FixityServiceOptions.Mode.ALL_IN_ONE_LIST.getKey());
        props.put("hashApproach",
                  FixityServiceOptions.HashApproach.STORED.toString());
        props.put("salt", "");
        props.put("failFast", Boolean.FALSE.toString());
        props.put("storeId", storeIdS3);
        props.put("targetSpaceId", null);
        props.put("providedListingSpaceIdA", adminSpaceId);
        props.put("providedListingContentIdA", outputGenContentId);
        props.put("outputSpaceId", adminSpaceId);
        props.put("outputContentId", outputAllInOneContentId);
        props.put("reportContentId", outputReportId);

        props.put("username", getRoot().getUsername());
        props.put("password", getRoot().getPassword());
        props.put("duraStorePort", port);

        configAdmin.updateConfiguration(getConfigId(), props);
    }

    private String getConfigId() {
        return "fixityservice-" + this.version + ".zip";
    }

    private void waitForConfigMode(String expected) {
        String mode = null;
        int MAX_TRIES = 10;
        int tries = 0;
        while ((mode == null || !mode.equals(expected)) && tries < MAX_TRIES) {
            mode = fixity.getMode();
            sleep(100);
            tries++;
        }
        Assert.assertNotNull(mode);
        Assert.assertEquals(expected + " ? " + mode, expected, mode);
    }

    private void waitForCompletion(String phase) throws Exception {
        Map<String, String> props = null;
        ServiceResultListener.StatusMsg msg;
        int MAX_TRIES = 100;
        int tries = 0;
        boolean done = false;
        while (!done && tries < MAX_TRIES) {
            props = fixity.getServiceProps();
            Assert.assertNotNull(props);

            String status = props.get(ServiceResultProcessor.STATUS_KEY);
            if (status != null) {
                msg = new ServiceResultListener.StatusMsg(status);
                if (ServiceResultListener.State
                    .COMPLETE
                    .equals(msg.getState()) && msg.getPhase().equals(phase)) {
                    done = true;
                }
            }
            sleep(500);
            tries++;
        }

        Assert.assertTrue(props != null ? props.toString() : "too many tries: " + tries,
                          tries < MAX_TRIES);
        Assert.assertEquals(ComputeService.ServiceStatus.STARTED,
                            fixity.getServiceStatus());
    }

    private void verifySpace(final String spaceId) {
        StoreCaller<Boolean> spaceCaller = new StoreCaller<Boolean>() {
            @Override
            protected Boolean doCall() throws ContentStoreException {
                Map<String, AclType> acls = contentStore.getSpaceACLs(spaceId);
                return acls != null;
            }

            @Override
            public String getLogMessage() {
                StringBuilder sb = new StringBuilder("Error creating space: ");
                sb.append(spaceId);
                return sb.toString();
            }
        };
        Assert.assertTrue(spaceCaller.getLogMessage(), spaceCaller.call());
    }

    private void verifyContent(final Collection<ContentLocation> items) {
        StoreCaller<Boolean> caller = new StoreCaller<Boolean>() {
            @Override
            protected Boolean doCall() throws ContentStoreException {
                int good = 0;
                Map<String, String> properties;
                for (ContentLocation item : items) {
                    properties =
                        contentStore.getContentProperties(item.getSpaceId(),
                                                          item.getContentId());
                    if (properties != null) {
                        good++;
                        properties = null;
                    }
                }
                return good == items.size();
            }

            @Override
            public String getLogMessage() {
                return "Test content not properly created in space";
            }
        };
        Assert.assertTrue(caller.getLogMessage(), caller.call());
    }

    private void verifyGeneratedHashContent(String contentId) throws Exception {
        BufferedReader reader = getContentReader(contentId);
        verifyGeneratedHashes(reader);
    }

    private void verifyGeneratedHashes(BufferedReader reader)
        throws IOException {
        Map<ContentLocation, String> itemToMD5 = new HashMap<ContentLocation, String>();

        reader.readLine(); // not counting header line

        String line;
        int count = 0;
        while ((line = reader.readLine()) != null) {
            count++;

            String[] parts = line.split(",");
            Assert.assertEquals(3, parts.length);

            Assert.assertEquals(line, targetSpaceId, parts[0]);
            itemToMD5.put(new ContentLocation(parts[0], parts[1]), parts[2]);
        }
        reader.close();

        Assert.assertEquals(this.items.size(), count);

        // uses knowledge of how content was created in "beforeClass()" above.
        org.junit.Assert.assertTrue(this.items.size() > 0);
        for (ContentLocation item : this.items) {
            String md5 = itemToMD5.get(item);
            Assert.assertNotNull(md5);

            String expectedMd5 = getMd5("data-" + item.getContentId());
            Assert.assertEquals(expectedMd5, md5);
        }
    }

    protected String getMd5(String text) {
        return checksumUtil.generateChecksum(getContentStream(text));
    }

    private InputStream getContentStream(String text) {
        return new AutoCloseInputStream(new ByteArrayInputStream(text.getBytes()));
    }

    private void verifyReport(String contentId) throws Exception {
        BufferedReader reader = getContentReader(contentId);
        verifyCompareReport(reader);
    }

    private void verifyCompareReport(BufferedReader reader) throws IOException {

        Map<ContentLocation, String> itemToStatus = new HashMap<ContentLocation, String>();

        reader.readLine(); // not counting header line

        String line;
        int count = 0;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 0) {
                count++;

                String[] parts = line.split(",");
                Assert.assertEquals(5, parts.length);

                Assert.assertEquals(line, targetSpacePrefix, parts[0]);
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

            Assert.assertEquals("VALID", status);
        }
    }

    private BufferedReader getContentReader(String contentId) throws Exception {
        Content content = getContent(adminSpaceId, contentId);
        Assert.assertNotNull(adminSpaceId + "/" + contentId, content);

        return new BufferedReader(new InputStreamReader(content.getStream()));
    }

    private Content getContent(final String spaceId, final String contentId)
        throws Exception {
        StoreCaller<Content> caller = new StoreCaller<Content>() {
            @Override
            protected Content doCall() throws ContentStoreException {
                return contentStore.getContent(spaceId, contentId);
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

    private void tearDown() {
        deleteSpace(targetSpaceId);
        deleteSpace(adminSpaceId);
    }

    private void deleteSpace(final String spaceId) {
        StoreCaller<Boolean> caller = new StoreCaller<Boolean>() {
            @Override
            protected Boolean doCall() throws ContentStoreException {
                contentStore.deleteSpace(spaceId);
                return true;
            }

            @Override
            public String getLogMessage() {
                StringBuilder sb = new StringBuilder("Error deleting space: ");
                sb.append(spaceId);
                return sb.toString();
            }
        };
        Assert.assertTrue(caller.getLogMessage(), caller.call());
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private static int randomInt() {
        return new Random().nextInt(10000);
    }
}
