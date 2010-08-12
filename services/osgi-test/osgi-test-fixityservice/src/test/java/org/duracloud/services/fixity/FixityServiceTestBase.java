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
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.DuraCloudUserType;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.util.StoreCaller;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.duracloud.common.util.ChecksumUtil.Algorithm.MD5;

/**
 * @author Andrew Woods
 *         Date: Aug 10, 2010
 */
public class FixityServiceTestBase {


    protected FixityService fixity;
    private File workDir = new File("target/test-fixity-service");


    private String listingText;

    private final String listingSpaceId = "listing-space-id";
    private final String outputSpaceId = "output-space-id";
    private final String spaceId = "space-id";
    private final String contentId = "content-id-";
    private final String hash = "hash-";


    private static final int NUM_WORK_ITEMS = 10;
    private static List<String> spaceIds;
    protected static List<String> listingItems;
    protected static Map<String, String> contentIdToMd5;

    protected static final ChecksumUtil checksumUtil = new ChecksumUtil(MD5);
    protected static final String adminSpacePrefix = "test-fixity-admin-";
    protected static final String targetSpacePrefix = "test-fixity-target-";
    protected static final String targetContentPrefix = "test/content/id-";

    protected static final String listingContentId = "listing-content-id";

    private static final String DEFAULT_PORT = "8080";
    private static final String context = "durastore";
    private static final String host = "localhost";
    private static String port;

    @BeforeClass
    public static void beforeClass() throws Exception {
        final ContentStore store = createContentStore();

        spaceIds = new ArrayList<String>();
        listingItems = new ArrayList<String>();
        contentIdToMd5 = new HashMap<String, String>();

        // create input/output space
        final String adminSpaceId = getSpaceId(adminSpacePrefix);
        boolean success = false;
        int tries = 0;
        final int MAX_TRIES = 4;
        while (!success && tries < MAX_TRIES) {
            try {
                store.createSpace(adminSpaceId, null);
                success = true;
            } catch (ContentStoreException e) {
                Thread.sleep(1000);
                tries++;
            }
        }

        // create target space
        final String targetSpaceId = getSpaceId(targetSpacePrefix);
        success = false;
        tries = 0;
        try {
            store.createSpace(targetSpaceId, null);
            success = true;
        } catch (ContentStoreException e) {
            Thread.sleep(1000);
            tries++;
        }

        // create test content in target spaces
        for (int i = 0; i < NUM_WORK_ITEMS; ++i) {
            String contentId = targetContentPrefix + i;
            String text = "data-" + contentId;
            String md5 = getMd5(text);

            store.addContent(targetSpaceId,
                             contentId,
                             getContentStream(text),
                             text.length(),
                             "text/plain",
                             md5,
                             null);

            contentIdToMd5.put(contentId, md5);
        }

        // create input content listing, and space listing
        StringBuilder listing = new StringBuilder("space-id,content-id,md5");
        listing.append(System.getProperty("line.separator"));
        int i = 0;
        for (String contentId : contentIdToMd5.keySet()) {
            if (i % 3 == 0) {
                listingItems.add(contentId);

                listing.append(targetSpaceId);
                listing.append(",");
                listing.append(contentId);
                listing.append(",");
                listing.append(contentIdToMd5.get(contentId));
                listing.append(System.getProperty("line.separator"));
            }
            ++i;
        }

        store.addContent(adminSpaceId,
                         listingContentId,
                         getContentStream(listing.toString()),
                         listing.length(),
                         "text/plain",
                         getMd5(listing.toString()),
                         null);

        // verify spaces and content
        verifySpaces(store);
        verifyContent(store, targetSpaceId);
        verifyContent(store, adminSpaceId, listingContentId);
    }

    private static void verifySpaces(final ContentStore store) {
        StoreCaller<Boolean> spaceCaller = new StoreCaller<Boolean>() {
            @Override
            protected Boolean doCall() throws ContentStoreException {
                int good = 0;
                ContentStore.AccessType access;
                for (String spaceId : spaceIds) {
                    access = store.getSpaceAccess(spaceId);
                    if (access != null) {
                        good++;
                    }
                }
                return good == spaceIds.size();
            }

            @Override
            public String getLogMessage() {
                StringBuilder sb = new StringBuilder(
                    "Test spaces not properly created: ");
                for (String spaceId : spaceIds) {
                    sb.append(spaceId);
                    sb.append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
                return sb.toString();
            }
        };
        Assert.assertTrue(spaceCaller.getLogMessage(), spaceCaller.call());
    }

    private static void verifyContent(final ContentStore store,
                                      final String targetSpaceId) {
        StoreCaller<Boolean> contentCaller = new StoreCaller<Boolean>() {
            @Override
            protected Boolean doCall() throws ContentStoreException {
                int good = 0;
                Map<String, String> metadata;
                for (String contentId : contentIdToMd5.keySet()) {
                    metadata = store.getContentMetadata(targetSpaceId,
                                                        contentId);
                    if (metadata != null) {
                        good++;
                        metadata = null;
                    }
                }
                return good == contentIdToMd5.size();
            }

            @Override
            public String getLogMessage() {
                StringBuilder sb = new StringBuilder(
                    "Test content not properly created in space: ");
                sb.append(targetSpaceId);
                return sb.toString();
            }
        };
        Assert.assertTrue(contentCaller.getLogMessage(), contentCaller.call());
    }

    private static void verifyContent(final ContentStore store,
                                      final String spaceId,
                                      final String contentId) {
        StoreCaller<Boolean> listingCaller = new StoreCaller<Boolean>() {
            @Override
            protected Boolean doCall() throws ContentStoreException {
                Map<String, String> metadata = null;
                metadata = store.getContentMetadata(spaceId, contentId);
                return (null != metadata);
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
        Assert.assertTrue(listingCaller.getLogMessage(), listingCaller.call());
    }

    private static String getSpaceId(String prefix) {
        int id = new Random().nextInt(10000);
        String spaceId = prefix + id;
        spaceIds.add(spaceId);

        return spaceId;
    }

    @AfterClass
    public static void afterClass() throws Exception {
        ContentStore store = createContentStore();

        for (String spaceId : spaceIds) {
            try {
                store.deleteSpace(spaceId);
            } catch (ContentStoreException e) {
                System.out.println(
                    "Error deleting space: " + spaceId + ", " + e.getMessage());
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        fixity = new FixityService();

        if (!workDir.exists()) {
            Assert.assertTrue(workDir.getCanonicalPath(), workDir.mkdir());
        }

        fixity.setThreads(3);
        fixity.setServiceWorkDir(workDir.getCanonicalPath());
        fixity.setContentStore(createContentStore());
    }

    protected static ContentStore createContentStore() throws Exception {
        ContentStoreManager storeManager = new ContentStoreManagerImpl(host,
                                                                       getPort(),
                                                                       context);
        storeManager.login(getRootCredential());
        return storeManager.getPrimaryContentStore();
    }

    private static InputStream getContentStream(String text) {
        return new AutoCloseInputStream(new ByteArrayInputStream(text.getBytes()));
    }

    protected static String getMd5(String text) {
        return checksumUtil.generateChecksum(getContentStream(text));
    }

    protected String findSpaceId(String prefix) {
        String id = null;
        for (String spaceId : spaceIds) {
            if (spaceId.startsWith(prefix)) {
                id = spaceId;
            }
        }
        Assert.assertNotNull(id);
        return id;
    }

    private static String getPort() throws Exception {
        if (port == null) {
            FixityServiceTestConfig config = new FixityServiceTestConfig();
            port = config.getPort();
        }

        try { // Ensure the port is a valid port value
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            port = DEFAULT_PORT;
        }

        return port;
    }

    private static Credential getRootCredential() throws Exception {
        UnitTestDatabaseUtil dbUtil = new UnitTestDatabaseUtil();
        ResourceType rootUser = ResourceType.fromDuraCloudUserType(
            DuraCloudUserType.ROOT);
        return dbUtil.findCredentialForResource(rootUser);
    }
}
