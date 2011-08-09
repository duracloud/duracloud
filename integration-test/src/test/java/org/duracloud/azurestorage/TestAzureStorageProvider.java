/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.azurestorage;

import junit.framework.Assert;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProvider.AccessType;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static junit.framework.Assert.*;
import static org.duracloud.storage.util.StorageProviderUtil.*;

/**
 * Tests the Azure Storage Provider
 *
 * @author Kristen Cannava
 */
public class TestAzureStorageProvider {

    protected static final Logger log =
            LoggerFactory.getLogger(TestAzureStorageProvider.class);

    AzureStorageProvider azureProvider;

    private static String SPACE_ID = null;
    private static final String CONTENT_ID = "duracloud test content";
    private static final String SPACE_PROPS_NAME = "custom-space-properties";
    private static final String SPACE_PROPS_VALUE = "Testing Space";
    private static final String CONTENT_PROPS_NAME = "custom-content-properties";
    private static final String CONTENT_PROPS_VALUE = "Testing Content";
    private static final String CONTENT_MIME_NAME =
        StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
    private static final String CONTENT_MIME_VALUE = "text/plain";
    private static final String CONTENT_DATA = "Test Content";

    @Before
    public void setUp() throws Exception {
        Credential credential = getCredential();
        Assert.assertNotNull(credential);

        String username = credential.getUsername();
        String password = credential.getPassword();
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);

        azureProvider = new AzureStorageProvider(username, password);

        String random = String.valueOf(new Random().nextInt(99999));
        SPACE_ID = "duracloud-test-bucket-" + random;
    }

    @After
    public void tearDown() {
        try {
            azureProvider.deleteSpace(SPACE_ID);
        } catch (Exception e) {
            // Ignore, the space has likely already been deleted
        }
        azureProvider = null;
    }

    private Credential getCredential() throws Exception {
        UnitTestDatabaseUtil dbUtil = new UnitTestDatabaseUtil();
        return dbUtil.findCredentialForResource(ResourceType.fromStorageProviderType(
                StorageProviderType.MICROSOFT_AZURE));
    }

    @Test
    public void testAzureStorageProvider() throws Exception {
        int count = 0;
        /* Test Spaces */

        // test createSpace()
        log.debug("Test createSpace()");
        azureProvider.createSpace(SPACE_ID);
        testSpaceProperties(SPACE_ID, AccessType.CLOSED, count);

        // test setSpaceProperties()
        log.debug("Test setSpaceProperties()");
        Map<String, String> spaceProperties = new HashMap<String, String>();
        spaceProperties.put(SPACE_PROPS_NAME, SPACE_PROPS_VALUE);
        azureProvider.setSpaceProperties(SPACE_ID, spaceProperties);

        // test getSpaceProperties()
        log.debug("Test getSpaceProperties()");
        Map<String, String> sProperties =
                testSpaceProperties(SPACE_ID, AccessType.CLOSED, count);
        assertTrue(sProperties.containsKey(SPACE_PROPS_NAME));
        assertEquals(SPACE_PROPS_VALUE, sProperties.get(SPACE_PROPS_NAME));

        // test getSpaces()
        log.debug("Test getSpaces()");
        Iterator<String> spaces = azureProvider.getSpaces();
        assertNotNull(spaces);
        // This will only work when SPACE_ID fits the Azure container naming conventions
        assertTrue(contains(spaces, SPACE_ID));

        // test setSpaceAccess()
        log.debug("Test setSpaceAccess(OPEN)");
        azureProvider.setSpaceAccess(SPACE_ID, AccessType.OPEN);

        // test getSpaceAccess()
        log.debug("Test getSpaceAccess()");
        AccessType access = azureProvider.getSpaceAccess(SPACE_ID);
        assertEquals(AccessType.OPEN, access);

        // test set space access via properties update
        log.debug("Test setSpaceProperties(Access) ");
        spaceProperties = new HashMap<String, String>();
        spaceProperties.put(StorageProvider.PROPERTIES_SPACE_ACCESS,
                AccessType.CLOSED.name());
        azureProvider.setSpaceProperties(SPACE_ID, spaceProperties);

        // test getSpaceAccess()
        log.debug("Test getSpaceAccess()");
        access = azureProvider.getSpaceAccess(SPACE_ID);
        assertEquals(access, AccessType.CLOSED);

        /* Test Content */

        // test addContent()
        log.debug("Test addContent()");
        addContent(SPACE_ID, CONTENT_ID, CONTENT_MIME_VALUE, false);
        testSpaceProperties(SPACE_ID, AccessType.CLOSED, ++count);

        // test getContentProperties()
        log.debug("Test getContentProperties()");
        Map<String, String> cProperties =
                azureProvider.getContentProperties(SPACE_ID, CONTENT_ID);
        assertNotNull(cProperties);
        assertEquals(CONTENT_MIME_VALUE, cProperties.get(CONTENT_MIME_NAME));
        assertNotNull(cProperties.get(StorageProvider.PROPERTIES_CONTENT_MODIFIED));
        assertNotNull(cProperties.get(StorageProvider.PROPERTIES_CONTENT_SIZE));
        assertNotNull(cProperties.get(StorageProvider.PROPERTIES_CONTENT_CHECKSUM));

        // add additional content for getContents tests
        String testContent2 = "test-content-2";
        addContent(SPACE_ID, testContent2, CONTENT_MIME_VALUE, false);
        testSpaceProperties(SPACE_ID, AccessType.CLOSED, ++count);
        String testContent3 = "test-content-3";
        addContent(SPACE_ID, testContent3, null, true);
        testSpaceProperties(SPACE_ID, AccessType.CLOSED, ++count);

        // test getSpaceContents()
        log.debug("Test getSpaceContents()");
        Iterator<String> spaceContents =
                azureProvider.getSpaceContents(SPACE_ID, null);
        assertNotNull(spaceContents);
        assertEquals(3, count(spaceContents));

        // test getSpaceContentsChunked() maxLimit
        log.debug("Test getSpaceContentsChunked() maxLimit");
        List<String> spaceContentList =
                azureProvider.getSpaceContentsChunked(SPACE_ID,
                        null,
                        2,
                        null);
        assertNotNull(spaceContentList);
        assertEquals(2, spaceContentList.size());
        String lastItem = spaceContentList.get(spaceContentList.size() - 1);
        spaceContentList = azureProvider.getSpaceContentsChunked(SPACE_ID,
                null,
                2,
                lastItem);
        assertNotNull(spaceContentList);
        assertEquals(1, spaceContentList.size());

        // test getSpaceContentsChunked() prefix
        log.debug("Test getSpaceContentsChunked() prefix");
        spaceContentList = azureProvider.getSpaceContentsChunked(SPACE_ID,
                "test",
                10,
                null);
        assertEquals(2, spaceContentList.size());

        // test getContent()
        log.debug("Test getContent()");
        InputStream is = azureProvider.getContent(SPACE_ID, CONTENT_ID);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String contentLine = reader.readLine();
        assertNotNull(contentLine);
        assertTrue(contentLine.equals(CONTENT_DATA));

        // test invalid content
        log.debug("Test getContent() with invalid content ID");
        log.debug("-- Begin expected error log -- ");
        try {
            azureProvider.getContent(SPACE_ID, "non-existant-content");
            fail("Exception expected");
        } catch (Exception e) {
            assertNotNull(e);
        }
        log.debug("-- End expected error log --");

        // test setContentProperties()
        log.debug("Test setContentProperties()");
        Map<String, String> contentProperties = new HashMap<String, String>();
        contentProperties.put(CONTENT_PROPS_NAME, CONTENT_PROPS_VALUE);
        azureProvider.setContentProperties(SPACE_ID,
                CONTENT_ID,
                contentProperties);

        // test getContentProperties()
        log.debug("Test getContentProperties()");
        cProperties = azureProvider.getContentProperties(SPACE_ID, CONTENT_ID);
        assertNotNull(cProperties);
        assertEquals(CONTENT_PROPS_VALUE, cProperties.get(CONTENT_PROPS_NAME));
        // Mime type was not included when setting content properties
        // so its value should have been maintained
        assertEquals(CONTENT_MIME_VALUE, cProperties.get(CONTENT_MIME_NAME));

        // test setContentProperties() - mimetype
        log.debug("Test setContentProperties() - mimetype");
        String newMime = "image/bmp";
        contentProperties = new HashMap<String, String>();
        contentProperties.put(CONTENT_MIME_NAME, newMime);
        azureProvider.setContentProperties(SPACE_ID,
                CONTENT_ID,
                contentProperties);
        cProperties = azureProvider.getContentProperties(SPACE_ID, CONTENT_ID);
        assertNotNull(cProperties);
        assertEquals(newMime, cProperties.get(CONTENT_MIME_NAME));
        // Custom properties was not included in update, it should be removed
        assertNull(cProperties.get(CONTENT_PROPS_NAME));

        log.debug("Test getContentProperties() - mimetype default");
        cProperties = azureProvider.getContentProperties(SPACE_ID, testContent3);
        assertNotNull(cProperties);
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                cProperties.get(CONTENT_MIME_NAME));

        /* Test Deletes */

        // test deleteContent()
        log.debug("Test deleteContent()");
        azureProvider.deleteContent(SPACE_ID, CONTENT_ID);
        spaceContents = azureProvider.getSpaceContents(SPACE_ID, null);
        assertFalse(contains(spaceContents, CONTENT_ID));
        testSpaceProperties(SPACE_ID, AccessType.CLOSED, --count);

        // test deleteSpace()
        log.debug("Test deleteSpace()");
        azureProvider.deleteSpace(SPACE_ID);
        spaces = azureProvider.getSpaces();
        assertFalse(contains(spaces, SPACE_ID));
    }

    private void addContent(String spaceId,
                            String contentId,
                            String mimeType,
                            boolean checksumInAdvance) {
        byte[] content = CONTENT_DATA.getBytes();
        int contentSize = content.length;
        ByteArrayInputStream contentStream = new ByteArrayInputStream(content);

        String advChecksum = null;
        if (checksumInAdvance) {
            ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
            advChecksum = util.generateChecksum(contentStream);
            contentStream.reset();
        }

        String checksum = azureProvider.addContent(spaceId,
                contentId,
                mimeType,
                contentSize,
                advChecksum,
                contentStream);

        if (checksumInAdvance) {
            assertEquals(advChecksum, checksum);
        }

        compareChecksum(azureProvider, spaceId, contentId, checksum);
    }

    private Map<String, String> testSpaceProperties(String spaceId,
                                                  AccessType access,
                                                  int count) {
        Map<String, String> sProperties =
                azureProvider.getSpaceProperties(spaceId);

        assertTrue(sProperties.containsKey(
                StorageProvider.PROPERTIES_SPACE_CREATED));
        assertNotNull(sProperties.get(StorageProvider.PROPERTIES_SPACE_CREATED));

        assertTrue(sProperties.containsKey(
                StorageProvider.PROPERTIES_SPACE_COUNT));
        assertNotNull(sProperties.get(StorageProvider.PROPERTIES_SPACE_COUNT));
        assertEquals(String.valueOf(count),
                     sProperties.get(StorageProvider.PROPERTIES_SPACE_COUNT));
        
        assertTrue(sProperties.containsKey(
                StorageProvider.PROPERTIES_SPACE_ACCESS));
        String spaceAccess =
                sProperties.get(StorageProvider.PROPERTIES_SPACE_ACCESS);
        assertNotNull(spaceAccess);
        assertEquals(access.name(), spaceAccess);
        assertNotNull(sProperties.get(StorageProvider.PROPERTIES_SPACE_ACCESS));


        return sProperties;
    }

    @Test
    public void testNotFound() {
        String spaceId = SPACE_ID;
        String contentId = "NonExistantContent";
        String failMsg = "Should throw NotFoundException attempting to " +
                "access a space which does not exist";
        byte[] content = CONTENT_DATA.getBytes();

        // Space Not Found

        try {
            azureProvider.getSpaceProperties(spaceId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            azureProvider.setSpaceProperties(spaceId,
                    new HashMap<String, String>());
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            azureProvider.getSpaceContents(spaceId, null);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            azureProvider.getSpaceContentsChunked(spaceId, null, 100, null);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            azureProvider.getSpaceAccess(spaceId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            azureProvider.setSpaceAccess(spaceId, AccessType.CLOSED);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            azureProvider.deleteSpace(spaceId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            int contentSize = content.length;
            ByteArrayInputStream contentStream = new ByteArrayInputStream(
                    content);
            azureProvider.addContent(spaceId,
                    contentId,
                    "text/plain",
                    contentSize,
                    null,
                    contentStream);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            azureProvider.getContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            azureProvider.getContentProperties(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            azureProvider.setContentProperties(spaceId,
                    contentId,
                    new HashMap<String, String>());
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            azureProvider.deleteContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        // Content Not Found

        azureProvider.createSpace(spaceId);
        failMsg = "Should throw NotFoundException attempting to " +
                "access content which does not exist";

        try {
            azureProvider.getContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            azureProvider.getContentProperties(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            azureProvider.setContentProperties(spaceId,
                    contentId,
                    new HashMap<String, String>());
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            azureProvider.deleteContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }
    }

}
