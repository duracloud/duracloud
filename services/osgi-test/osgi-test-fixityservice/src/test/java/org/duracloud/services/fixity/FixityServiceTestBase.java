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
import org.duracloud.common.model.AclType;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.DuraCloudUserType;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.util.StoreCaller;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.duracloud.unittestdb.util.StorageAccountTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
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

    private static final int NUM_WORK_ITEMS = 10;
    private static List<String> spaceIds;
    protected static List<ContentLocation> listingItems;
    protected static List<ContentLocation> allItems0;
    protected static Map<ContentLocation, String> itemToMd5;
    protected static Map<ContentLocation, String> corruptItemsToStatus;

    protected static final ChecksumUtil checksumUtil = new ChecksumUtil(MD5);
    protected static final String adminSpacePrefix = "test-fixity-admin-";
    protected static final String targetSpacePrefix0 = "test-fixity-target-0-";
    protected static final String targetSpacePrefix1 = "test-fixity-target-1-";
    protected static final String targetContentPrefix = "test/content/id-";

    protected static final String allContentId0 = "all-content-0-id";
    protected static final String listingContentId = "listing-content-id";
    protected static final String listingGoodContentId = "listing-good-content-id";
    protected static final String listingBadContentId = "listing-bad-content-id";

    private static final String DEFAULT_PORT = "8080";
    private static final String context = "durastore";
    private static final String host = "localhost";
    private static String port;

    @BeforeClass
    public static void beforeClass() throws Exception {
        initializeDurastore();

        final ContentStore store = createContentStore();

        spaceIds = new ArrayList<String>();
        itemToMd5 = new HashMap<ContentLocation, String>();
        corruptItemsToStatus = new HashMap<ContentLocation, String>();

        // create input/output space
        final String adminSpaceId = getSpaceId(adminSpacePrefix);
        createSpace(store, adminSpaceId);

        // create target space
        final String targetSpaceId0 = getSpaceId(targetSpacePrefix0);
        final String targetSpaceId1 = getSpaceId(targetSpacePrefix1);
        createSpace(store, targetSpaceId0);
        createSpace(store, targetSpaceId1);

        // create test content in target space
        for (int i = 0; i < NUM_WORK_ITEMS; ++i) {
            String contentId = targetContentPrefix + i;
            String text = "data-" + contentId;
            String md5 = getMd5(text);

            addContent(store, targetSpaceId0, contentId, text, md5);
            addContent(store, targetSpaceId1, contentId, text, md5);

            itemToMd5.put(new ContentLocation(targetSpaceId0, contentId), md5);
            itemToMd5.put(new ContentLocation(targetSpaceId1, contentId), md5);
        }

        // create input content listing
        allItems0 = new ArrayList<ContentLocation>();
        for (ContentLocation item : itemToMd5.keySet()) {
            if (item.getSpaceId().startsWith(targetSpacePrefix0)) {
                allItems0.add(item);
            }
        }

        int modulus = 3;
        listingItems = createListingItems(itemToMd5.keySet(), modulus);

        boolean isCorrupt = true;
        String allListing0 = createListing(allItems0, !isCorrupt);
        String listing = createListing(listingItems, !isCorrupt);
        String badListing = createListing(listingItems, isCorrupt);

        addContent(store,
                   adminSpaceId,
                   allContentId0,
                   allListing0.toString(),
                   getMd5(allListing0.toString()));

        addContent(store,
                   adminSpaceId,
                   listingContentId,
                   listing.toString(),
                   getMd5(listing.toString()));

        addContent(store,
                   adminSpaceId,
                   listingGoodContentId,
                   listing.toString(),
                   getMd5(listing.toString()));

        addContent(store,
                   adminSpaceId,
                   listingBadContentId,
                   badListing.toString(),
                   getMd5(badListing.toString()));

        // verify spaces and content
        verifySpaces(store);
        verifyContent(store, itemToMd5.keySet());
        verifyContent(store, adminSpaceId, allContentId0);
        verifyContent(store, adminSpaceId, listingContentId);
        verifyContent(store, adminSpaceId, listingGoodContentId);
        verifyContent(store, adminSpaceId, listingBadContentId);
    }

    private static void initializeDurastore() throws Exception {
        StorageAccountTestUtil acctUtil = new StorageAccountTestUtil();
        acctUtil.initializeDurastore(host, getPort(), context);
    }

    private static void addContent(final ContentStore store,
                                   final String spaceId,
                                   final String contentId,
                                   final String text,
                                   final String md5) {
        StoreCaller<Boolean> caller = new StoreCaller<Boolean>() {
            @Override
            protected Boolean doCall() throws ContentStoreException {
                String newMd5 = store.addContent(spaceId,
                                                 contentId,
                                                 getContentStream(text),
                                                 text.length(),
                                                 "text/plain",
                                                 md5,
                                                 null);
                return md5.equals(newMd5);
            }

            @Override
            public String getLogMessage() {
                StringBuilder sb = new StringBuilder("Error adding content: ");
                sb.append(spaceId);
                sb.append("/");
                sb.append(contentId);
                return sb.toString();
            }
        };
        Assert.assertTrue(caller.getLogMessage(), caller.call());
    }

    private static void createSpace(ContentStore store, String spaceId)
        throws InterruptedException {
        boolean success = false;
        int tries = 0;
        final int MAX_TRIES = 4;
        while (!success && tries < MAX_TRIES) {
            try {
                store.createSpace(spaceId);
                success = true;
            } catch (ContentStoreException e) {
                Thread.sleep(1000);
                tries++;
            }
        }
    }

    private static List<ContentLocation> createListingItems(Collection<ContentLocation> allItems,
                                                            int modulus) {
        List<ContentLocation> resultItems = new ArrayList<ContentLocation>();
        for (ContentLocation item : allItems) {
            String contentId = item.getContentId();
            String suffix = contentId.substring(targetContentPrefix.length(),
                                                contentId.length());
            int index = Integer.parseInt(suffix);

            if (index % modulus == 0) {
                resultItems.add(item);
            }
        }
        return resultItems;
    }

    private static String createListing(List<ContentLocation> items,
                                        boolean isCorrupt) {
        StringBuilder listing = new StringBuilder("space-id,content-id,md5");
        listing.append(System.getProperty("line.separator"));
        int i = 0;
        for (ContentLocation item : items) {
            listing.append(item.getSpaceId());
            listing.append(",");
            listing.append(item.getContentId());
            listing.append(",");
            if (isCorrupt && i == 3) {
                listing.append("junk-md5");
                corruptItemsToStatus.put(item, "MISMATCH");
            } else {
                listing.append(itemToMd5.get(item));
            }
            listing.append(System.getProperty("line.separator"));

            ++i;
        }
        return listing.toString();
    }

    private static void verifySpaces(final ContentStore store) {
        StoreCaller<Boolean> spaceCaller = new StoreCaller<Boolean>() {
            @Override
            protected Boolean doCall() throws ContentStoreException {
                int good = 0;
                Map<String, AclType> acls;
                for (String spaceId : spaceIds) {
                    acls = store.getSpaceACLs(spaceId);
                    if (acls != null) {
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
                                      final Collection<ContentLocation> items) {
        StoreCaller<Boolean> contentCaller = new StoreCaller<Boolean>() {
            @Override
            protected Boolean doCall() throws ContentStoreException {
                int good = 0;
                Map<String, String> properties;
                for (ContentLocation item : items) {
                    properties = store.getContentProperties(item.getSpaceId(),
                                                            item.getContentId());
                    if (properties != null) {
                        good++;
                        properties = null;
                    }
                }
                return good == itemToMd5.size();
            }

            @Override
            public String getLogMessage() {
                StringBuilder sb = new StringBuilder(
                    "Test content not properly created in space");
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
                Map<String, String> properties = null;
                properties = store.getContentProperties(spaceId, contentId);
                return (null != properties);
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
