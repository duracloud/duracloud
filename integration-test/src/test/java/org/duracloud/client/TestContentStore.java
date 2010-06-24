/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import junit.framework.Assert;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.DuraCloudUserType;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.InvalidIdException;
import org.duracloud.error.NotFoundException;
import org.duracloud.error.UnsupportedTaskException;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.duracloud.unittestdb.util.StorageAccountTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Runtime test of DuraCloud java client.
 *
 * @author Bill Branan
 */
public class TestContentStore {

    protected static final Logger log =
        LoggerFactory.getLogger(TestContentStore.class);

    private static String host = "localhost";

    private static String port = null;

    private static final String defaultPort = "8080";

    private static String context = "durastore";

    private static ContentStoreManager storeManager;

    private static ContentStore store;

    private static String spaceId;

    private static List<String> spaces;

    static {
        String random = String.valueOf(new Random().nextInt(99999));
        spaceId = "storeclient-test-space-" + random;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        StorageAccountTestUtil acctUtil = new StorageAccountTestUtil();
        acctUtil.initializeDurastore(host, getPort(), context);

        storeManager = new ContentStoreManagerImpl(host, getPort(), context);
        storeManager.login(getRootCredential());

        store = storeManager.getPrimaryContentStore();

        spaces = new ArrayList<String>();
    }

    @Before
    public void setUp() throws Exception {
        if(!spaceExists(spaceId)) {
            // Create space
            Map<String, String> spaceMetadata = new HashMap<String, String>();
            spaceMetadata.put(ContentStore.SPACE_ACCESS,
                              ContentStore.AccessType.OPEN.name());
            createSpace(spaceId, spaceMetadata);
        }
    }

    private static String getPort() throws Exception {
        if (port == null) {
            StoreClientConfig config = new StoreClientConfig();
            port = config.getPort();
        }

        try { // Ensure the port is a valid port value
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            port = defaultPort;
        }

        return port;
    }

    @After
    public void tearDown() throws Exception {
        for(String spaceId : spaces) {
            if(spaceExists(spaceId)) {
                Iterator<String> contents = store.getSpaceContents(spaceId);
                while(contents.hasNext()) {
                    store.deleteContent(spaceId, contents.next());
                }
            }
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
        // Make sure the space is deleted
        for(String spaceId : spaces) {
            if(spaceExists(spaceId)) {
                store.deleteSpace(spaceId);
                log.info("Removed Space " + spaceId);                
            }

            int maxLoops = 10;
            for (int loops = 0;
                 spaceExists(spaceId) && loops < maxLoops;
                 loops++) {
                Thread.sleep(1000);
            }
        }
    }

    @Test
    public void testContentStoreManager() throws Exception {
        Map<String, ContentStore> contentStoreMap =
            storeManager.getContentStores();
        assertNotNull(contentStoreMap);
        assertFalse(contentStoreMap.isEmpty());
        ContentStore primaryStore = store;
        assertNotNull(primaryStore);
        Iterator<ContentStore> contentStores =
            contentStoreMap.values().iterator();
        boolean primaryInList = false;
        while(contentStores.hasNext()) {
            ContentStore store = contentStores.next();
            assertNotNull(store.getStoreId());
            assertNotNull(store.getStorageProviderType());
            if(store.getStoreId().equals(primaryStore.getStoreId())) {
                primaryInList = true;
                assertEquals(store.getStorageProviderType(),
                             primaryStore.getStorageProviderType());
            }

            ContentStore storeById =
                storeManager.getContentStore(store.getStoreId());
            assertNotNull(storeById);
            assertEquals(store.getStoreId(), storeById.getStoreId());
            assertEquals(store.getStorageProviderType(),
                         storeById.getStorageProviderType());
        }
        assertTrue(primaryInList);
    }

    @Test
    public void testAddSpace() throws Exception {
        // Test invalid space names

        List<String> invalidIds = new ArrayList<String>();

        invalidIds.add("Test-Space");  // Uppercase
        invalidIds.add("test-space!"); // Special character
        invalidIds.add("test..space"); // Multiple periods
        invalidIds.add("-test-space"); // Starting with a dash
        invalidIds.add("test-space-"); // Ending with a dash
        invalidIds.add("test-.space"); // Dash next to a period
        invalidIds.add("te");          // Too short
        invalidIds.add("test-space-test-space-test-space-" +
                       "test-space-test-space-test-spac)"); // Too long
        invalidIds.add("127.0.0.1");   // Formatted as an IP address

        for(String id : invalidIds) {
            checkInvalidSpaceId(id);
        }

        // Test valid space names

        String id = "test-space.test.space";
        checkValidSpaceId(id);

        id = "tes";
        checkValidSpaceId(id);

        id = "test-space-test-space-test-space-test-space-test-space-test-spa";
        checkValidSpaceId(id);
    }

    private void checkInvalidSpaceId(String id) throws Exception {
        try {
            createSpace(id, null);
            fail("Exception expected attempting to add " +
                 "a space with an invalid id: " + id);
        } catch(InvalidIdException e) {
            assertNotNull(e);
        }
    }

    private void checkValidSpaceId(String id) throws Exception {
        createSpace(id, null);
    }

    private void createSpace(String spaceId, Map<String, String> spaceMetadata)
        throws Exception {
        boolean spaceExists = spaceExists(spaceId);
        if(!spaceExists) {
            store.createSpace(spaceId, spaceMetadata);
            log.info("Created Space " + spaceId);

            int maxLoops = 20;
            for (int loops = 0;
                 !spaceExists && loops < maxLoops;
                 loops++) {
                Thread.sleep(2000);
                spaceExists = spaceExists(spaceId);
            }
        }

        if(!spaceExists) {
            fail("Attempt to create space " + spaceId + " failed.");
        }

        spaces.add(spaceId);
    }

    private static boolean spaceExists(String spaceId) throws Exception {
        try {
            store.getSpaceAccess(spaceId);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Test
    public void testSpaceMetadata() throws Exception {
        String metaName = "test-metadata";
        String metaValue = "test-value";

        // Check space
        List<String> spaces = store.getSpaces();
        assertNotNull(spaces);
        assertTrue(spaces.size() >= 1);
        assertTrue(spaces.contains(spaceId));

        Map<String, String> responseMetadata = store.getSpaceMetadata(spaceId);
        assertNotNull(responseMetadata);

        // Check space metadata
        assertNotNull(responseMetadata);
        assertEquals(ContentStore.AccessType.OPEN.name(),
                     responseMetadata.get(ContentStore.SPACE_ACCESS));
        assertNotNull(responseMetadata.get(ContentStore.SPACE_COUNT));
        assertNotNull(responseMetadata.get(ContentStore.SPACE_CREATED));
        assertEquals(ContentStore.AccessType.OPEN, store.getSpaceAccess(spaceId));

        // Set space metadata
        metaValue = "Testing Metadata Value";
        Map<String, String> spaceMetadata = new HashMap<String, String>();
        spaceMetadata.put(ContentStore.SPACE_ACCESS,
                          ContentStore.AccessType.CLOSED.name());
        spaceMetadata.put(metaName, metaValue);
        store.setSpaceMetadata(spaceId, spaceMetadata);

        // Check space metadata
        responseMetadata = store.getSpaceMetadata(spaceId);
        assertNotNull(responseMetadata);
        assertEquals(ContentStore.AccessType.CLOSED.name(),
                     responseMetadata.get(ContentStore.SPACE_ACCESS));
        assertNotNull(responseMetadata.get(ContentStore.SPACE_COUNT));
        assertNotNull(responseMetadata.get(ContentStore.SPACE_CREATED));
        assertEquals(metaValue, responseMetadata.get(metaName));
        assertEquals(ContentStore.AccessType.CLOSED,
                     store.getSpaceAccess(spaceId));

        // Set space access
        store.setSpaceAccess(spaceId, ContentStore.AccessType.OPEN);
        assertEquals(ContentStore.AccessType.OPEN, store.getSpaceAccess(spaceId));
    }

    @Test
    public void testInvalidSpace() throws Exception {
        String invalidSpaceId = "invalid-space-id";
        Map<String, String> emptyMap = new HashMap<String, String>();

        // Ensure invalid space is not in spaces listing
        List<String> spaces = store.getSpaces();
        assertNotNull(spaces);
        assertTrue(spaces.size() >= 1);
        assertFalse(spaces.contains(invalidSpaceId));

        try {
            store.deleteSpace(invalidSpaceId);
            fail("Exception expected on deleteSpace(invalidSpaceId)");
        } catch(NotFoundException expected) {
        }

        try {
            store.getSpaceMetadata(invalidSpaceId);
            fail("Exception expected on getSpace(invalidSpaceId)");
        } catch(NotFoundException expected) {
        }

        try {
            store.getSpaceAccess(invalidSpaceId);
            fail("Exception expected on getSpaceAccess(invalidSpaceId)");
        } catch(NotFoundException expected) {
        }

        try {
            store.setSpaceAccess(invalidSpaceId, ContentStore.AccessType.OPEN);
            fail("Exception expected on setSpaceAccess(invalidSpaceId)");
        } catch(NotFoundException expected) {
        }

        try {
            store.getSpaceMetadata(invalidSpaceId);
            fail("Exception expected on getSpaceMetadata(invalidSpaceId)");
        } catch(NotFoundException expected) {
        }

        try {
            store.setSpaceMetadata(invalidSpaceId, emptyMap);
            fail("Exception expected on setSpaceMetadata(invalidSpaceId)");
        } catch(NotFoundException expected) {
        }

        try {
            String contentId = "test-content";
            String content = "This is the information stored as content";
            InputStream contentStream = IOUtil.writeStringToStream(content);
            String contentMimeType = "text/plain";
            store.addContent(invalidSpaceId, contentId, contentStream,
                             content.length(), contentMimeType, null, emptyMap);
            fail("Exception expected on addContent(invalidSpaceId, ...)");
        } catch(NotFoundException expected) {
        }

        try {
            String contentId = "test-content";
            store.getContent(invalidSpaceId, contentId);
            fail("Exception expected on getContent(invalidSpaceId, ...)");
        } catch(NotFoundException expected) {
        }

        try {
            String contentId = "test-content";
            store.deleteContent(invalidSpaceId, contentId);
            fail("Exception expected on deleteContent(invalidSpaceId, ...)");
        } catch(NotFoundException expected) {
        }

        try {
            String contentId = "test-content";
            store.getContentMetadata(invalidSpaceId, contentId);
            fail("Exception expected on getContentMetadata(invalidSpaceId, ...)");
        } catch(NotFoundException expected) {
        }

        try {
            String contentId = "test-content";
            store.setContentMetadata(invalidSpaceId, contentId, emptyMap);
            fail("Exception expected on setContentMetadata(invalidSpaceId, ...)");
        } catch(NotFoundException expected) {
        }
    }

    @Test
    public void testSpaceContents() throws Exception {
        String mime = "text/plain";

        // Add content
        String c1 = "test-content-1";
        InputStream stream = IOUtil.writeStringToStream(c1);
        store.addContent(spaceId, c1, stream, c1.length(), mime, null, null);
        String c2 = "test-content-2";
        stream = IOUtil.writeStringToStream(c2);
        store.addContent(spaceId, c2, stream, c2.length(), mime, null, null);
        String c3 = "content-3";
        stream = IOUtil.writeStringToStream(c3);
        store.addContent(spaceId, c3, stream, c3.length(), mime, null, null);

        // Get all content
        Iterator<String> contentIds = store.getSpaceContents(spaceId);
        int count = 0;
        while(contentIds.hasNext()) {
            String contentId = contentIds.next();
            assertTrue(contentId.equals(c1) ||
                       contentId.equals(c2) ||
                       contentId.equals(c3));
            count++;
        }
        assertEquals(3, count);

        // Get content with prefix
        contentIds = store.getSpaceContents(spaceId, "test");
        count = 0;
        while(contentIds.hasNext()) {
            String contentId = contentIds.next();
            assertTrue(contentId.equals(c1) ||
                       contentId.equals(c2));
            count++;
        }
        assertEquals(2, count);

        // Chunk content list
        List<String> idList =
            store.getSpace(spaceId, null, 2, null).getContentIds();
        assertEquals(2, idList.size());
        String lastItem = idList.get(idList.size()-1);
        idList = store.getSpace(spaceId, null, 2, lastItem).getContentIds();
        assertEquals(1, idList.size());
    }

    @Test
    public void testAddContent() throws Exception {
        // Test invalid content IDs

        // Question mark
        String id = "test?content";
        testInvalidContentItem(id);

        // Backslash
        id = "test\\content";
        testInvalidContentItem(id);

        // Too long
        id = "test-content";
        while(id.getBytes().length <= 1024) {
            id += "test-content";
        }
        testInvalidContentItem(id);

        // Test valid content IDs

        // Test Special characters
        char[] specialChars = {'~','`','!','@','$','^','&','*','(',')','_','-',
                               '+','=','\'',':','.',',','<','>','"','[',']',
                               '{','}','#','%',';','|',' ','/'};
        for(char character : specialChars) {
            testCharacterInContentId(character);
        }
    }

    private void testInvalidContentItem(String contentId) throws Exception {
        try {
            addContentItem(contentId, false);
            fail("Exception expected attempting to add " +
                 "content with an invalid id");
        } catch (InvalidIdException e) {
            assertNotNull(e);
        }
    }

    private String addContentItem(String contentId, boolean checksumInAdvance)
        throws Exception {
        String content = "Test content";
        ByteArrayInputStream contentStream =
            new ByteArrayInputStream(content.getBytes());
        String contentMimeType = "text/plain";

        String advChecksum = null;
        if(checksumInAdvance) {
            ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
            advChecksum = util.generateChecksum(contentStream);
            contentStream.reset();
        }

        String checksum = store.addContent(spaceId,
                                           contentId,
                                           contentStream,
                                           content.length(),
                                           contentMimeType,
                                           advChecksum,
                                           null);

        if(checksumInAdvance) {
            Assert.assertEquals(advChecksum, checksum);
        }

        return checksum;
    }

    private void testCharacterInContentId(char character) throws Exception {
        String contentId = "test-" + String.valueOf(character) + "-content";
        String checksum = addContentItem(contentId, true);
        assertNotNull(checksum);
        
        Content content = store.getContent(spaceId, contentId);
        assertNotNull(content);
        assertEquals(contentId, content.getId());
    }

    @Test
    public void testContent() throws Exception {
        String contentId = "test-content";
        String content = "This is the information stored as content";
        InputStream contentStream = IOUtil.writeStringToStream(content);
        String contentMimeType = "text/plain";
        String metaName = "test-content-metadata";
        String metaValue = "Testing Content Metadata";
        Map<String, String> contentMetadata = new HashMap<String, String>();
        contentMetadata.put(metaName, metaValue);

        // Add content
        String checksum = store.addContent(spaceId,
                                           contentId,
                                           contentStream,
                                           content.length(),
                                           contentMimeType,
                                           null,
                                           contentMetadata);
        // Check content checksum
        assertNotNull(checksum);
        ChecksumUtil checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        contentStream = IOUtil.writeStringToStream(content);
        assertEquals(checksum, checksumUtil.generateChecksum(contentStream));

        // Check content
        Content responseContent = store.getContent(spaceId, contentId);
        assertNotNull(responseContent);
        assertEquals(content,
                     IOUtil.readStringFromStream(responseContent.getStream()));

        // Check content metadata
        Map<String, String> responseMetadata = responseContent.getMetadata();
        assertEquals(metaValue, responseMetadata.get(metaName));
        assertEquals(checksum,
                     responseMetadata.get(ContentStore.CONTENT_CHECKSUM));
        assertEquals(contentMimeType,
                     responseMetadata.get(ContentStore.CONTENT_MIMETYPE));
        assertEquals(String.valueOf(content.length()),
                     responseMetadata.get(ContentStore.CONTENT_SIZE));
        assertNotNull(responseMetadata.get(ContentStore.CONTENT_MODIFIED));

        // Set content metadata
        metaValue = "New Metadata Value";
        contentMetadata = new HashMap<String, String>();
        contentMetadata.put(metaName, metaValue);
        store.setContentMetadata(spaceId, contentId, contentMetadata);

        // Check content metadata
        responseMetadata = store.getContentMetadata(spaceId, contentId);
        assertEquals(metaValue, responseMetadata.get(metaName));
        assertEquals(checksum,
                     responseMetadata.get(ContentStore.CONTENT_CHECKSUM));
        assertEquals(contentMimeType,
                     responseMetadata.get(ContentStore.CONTENT_MIMETYPE));
        assertEquals(String.valueOf(content.length()),
                     responseMetadata.get(ContentStore.CONTENT_SIZE));
        assertNotNull(responseMetadata.get(ContentStore.CONTENT_MODIFIED));

        // Delete content
        store.deleteContent(spaceId, contentId);
        try {
            store.getContent(spaceId, contentId);
            fail("Exception should be thrown attempting to retrieve deleted content");
        } catch(ContentStoreException cse) {
            assertNotNull(cse.getMessage());
        }
    }

    @Test
    public void testInvalidContent() throws Exception {
        String invalidContentId = "invalid-content-id";
        Map<String, String> emptyMap = new HashMap<String, String>();

        // Create space
        Map<String, String> spaceMetadata = store.getSpaceMetadata(spaceId);
        assertNotNull(spaceMetadata);

        try {
            store.deleteContent(spaceId, invalidContentId);
            fail("Exception expected on deleteContent(spaceId, invalidContentId)");
        } catch(NotFoundException expected) {
        }

        try {
            store.getContent(spaceId, invalidContentId);
            fail("Exception expected on getContent(spaceId, invalidContentId)");
        } catch(NotFoundException expected) {
        }

        try {
            store.getContentMetadata(spaceId, invalidContentId);
            fail("Exception expected on getContentMetadata(spaceId, invalidContentId)");
        } catch(NotFoundException expected) {
        }

        try {
            store.setContentMetadata(spaceId, invalidContentId, emptyMap);
            fail("Exception expected on setContentMetadata(spaceId, invalidContentId, ...)");
        } catch(NotFoundException expected) {
        }
    }

    @Test
    public void testGetSupportedTasks() throws Exception {
        List<String> supportedTasks = store.getSupportedTasks();
        assertNotNull(supportedTasks);
        assertTrue(supportedTasks.contains("noop"));
    }

    @Test
    public void testPerformTask() throws Exception {
        // Noop task
        String taskName = "noop";
        String taskParams = "";

        String response = store.performTask(taskName, taskParams);
        assertNotNull(response);

        // Invalid tasks
        taskName = "invalid-task";
        taskParams = "invalid-task-params";

        try {            
            store.performTask(taskName, taskParams);
        } catch(UnsupportedTaskException expected) {
            assertNotNull(expected);
        }
    }

    private static Credential getRootCredential() throws Exception {
        UnitTestDatabaseUtil dbUtil = new UnitTestDatabaseUtil();
        ResourceType rootUser = ResourceType.fromDuraCloudUserType(
            DuraCloudUserType.ROOT);
        return dbUtil.findCredentialForResource(rootUser);
    }

}