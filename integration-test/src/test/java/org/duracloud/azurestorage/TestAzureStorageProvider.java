/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.azurestorage;

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    
    private final List<String> spaceIds = new ArrayList<String>();

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
    }

    @After
    public void tearDown() {
        clean();
        azureProvider = null;
    }

    private void clean() {
        for (String spaceId : spaceIds) {
            try {
                azureProvider.deleteSpace(spaceId);
            } catch (Exception e) {
                // do nothing.
            }
        }
    }

    private String getNewSpaceId() {
        String random = String.valueOf(new Random().nextInt(99999));
        String spaceId = "durastore-test-space-" + random;
        spaceIds.add(spaceId);
        return spaceId;
    }

    private String getNewContentId() {
        String random = String.valueOf(new Random().nextInt(99999));
        String contentId = "durastore-test-content-" + random;
        return contentId;
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
        String SPACE_ID = getNewSpaceId();

        // test createSpace()
        log.debug("Test createSpace()");
        azureProvider.createSpace(SPACE_ID);
        testSpaceProperties(SPACE_ID, count);

        // test getSpaceProperties()
        log.debug("Test getSpaceProperties()");
        Map<String, String> sProperties = testSpaceProperties(SPACE_ID, count);

        // test getSpaces()
        log.debug("Test getSpaces()");
        Iterator<String> spaces = azureProvider.getSpaces();
        assertNotNull(spaces);
        // This will only work when SPACE_ID fits the Azure container naming conventions
        assertTrue(contains(spaces, SPACE_ID));

        /* Test Content */

        // test addContent()
        log.debug("Test addContent()");
        String CONTENT_ID = getNewContentId();
        addContent(SPACE_ID, CONTENT_ID, CONTENT_MIME_VALUE, false);
        testSpaceProperties(SPACE_ID, ++count);

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
        testSpaceProperties(SPACE_ID, ++count);
        String testContent3 = "test-content-3";
        addContent(SPACE_ID, testContent3, null, true);
        testSpaceProperties(SPACE_ID, ++count);

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
        testSpaceProperties(SPACE_ID, --count);

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
                null,
                contentSize,
                advChecksum,
                contentStream);

        if (checksumInAdvance) {
            assertEquals(advChecksum, checksum);
        }
        
        waitForEventualConsistency(spaceId, contentId);

        // FIXME: Azure is not providing an MD5 in its content properties
        // compareChecksum(azureProvider, spaceId, contentId, checksum);
    }
    
     private void waitForEventualConsistency(String spaceId, String contentId) {
        final int maxTries = 10;
        int tries = 0;

        Map<String, String> props = null;
        while (null == props && tries++ < maxTries) {
            try {
                props = azureProvider.getContentProperties(spaceId, contentId);
            } catch (Exception e) {
                // do nothing
            }

            if (null == props) {
                sleep(tries * 500);
            }
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    private Map<String, String> testSpaceProperties(String spaceId, int count) {
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

        return sProperties;
    }

    @Test
    public void testNotFound() {
        String spaceId = getNewSpaceId();
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
                    null,
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

    @Test
    public void testCopyContentDifferentSpace() throws Exception {
        String srcSpaceId = getNewSpaceId();
        String destSpaceId = getNewSpaceId();

        String srcContentId = getNewContentId();
        String destContentId = getNewContentId();

        doTestCopyContent(srcSpaceId, srcContentId, destSpaceId, destContentId);
    }

    @Test
    public void testCopyContentSameSpaceSameName() throws Exception {
        String srcSpaceId = getNewSpaceId();

        String srcContentId = getNewContentId();

        try {
            doTestCopyContent(srcSpaceId,
                              srcContentId,
                              srcSpaceId,
                              srcContentId);
            Assert.fail("Azure does not support copy from/to same object");

        } catch (Exception e) {
            Assert.assertEquals(StorageException.class, e.getClass());
        }
    }

    @Test
    public void testCopyContentSameSpaceDifferentName() throws Exception {
        String srcSpaceId = getNewSpaceId();

        String srcContentId = getNewContentId();
        String destContentId = getNewContentId();

        doTestCopyContent(srcSpaceId, srcContentId, srcSpaceId, destContentId);
    }

    private void doTestCopyContent(String srcSpaceId,
                                   String srcContentId,
                                   String destSpaceId,
                                   String destContentId) throws Exception {
        azureProvider.createSpace(srcSpaceId);
        if (!srcSpaceId.equals(destSpaceId)) {
            azureProvider.createSpace(destSpaceId);
        }

        log.info("source     : {} / {}", srcSpaceId, srcContentId);
        log.info("destination: {} / {}", destSpaceId, destContentId);

        addContent(srcSpaceId, srcContentId, CONTENT_MIME_VALUE, false);

        ChecksumUtil cksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String cksum = cksumUtil.generateChecksum(CONTENT_DATA);

        Map<String, String> userProps = new HashMap<String, String>();
        userProps.put("name0", "value0");
        userProps.put("color", "green");
        userProps.put("state", "VA");

        setProperties(srcSpaceId, srcContentId, userProps);
        Map<String, String> props = azureProvider.getContentProperties(
            srcSpaceId,
            srcContentId);
        verifyContent(srcSpaceId,
                      srcContentId,
                      cksum,
                      props,
                      userProps.keySet());

        String md5 = azureProvider.copyContent(srcSpaceId,
                                               srcContentId,
                                               destSpaceId,
                                               destContentId);
        Assert.assertNotNull(md5);

        // FIXME: Azure does not provide md5 guarantees.
        // Assert.assertEquals(cksum, md5);

        verifyContent(destSpaceId,
                      destContentId,
                      md5,
                      props,
                      userProps.keySet());
    }

    private void setProperties(String spaceId,
                               String contentId,
                               Map<String, String> userProps) {
        azureProvider.setContentProperties(spaceId, contentId, userProps);

        final int maxTries = 10;
        int tries = 0;
        boolean verified = false;
        Map<String, String> props = null;

        while (null == props && !verified && tries++ < maxTries) {
            try {
                props = azureProvider.getContentProperties(spaceId, contentId);
            } catch (Exception e) {
                // do nothing.
            }

            // verify prop update
            if (null != props) {
                for (String key : userProps.keySet()) {
                    verified = true;
                    if (!props.containsKey(key)) {
                        verified = false;
                    }
                }
            }

            // rest a moment
            if (!verified) {
                sleep(tries * 500);
            }
        }
    }

    private void verifyContent(String spaceId,
                               String contentId,
                               String md5,
                               Map<String, String> props,
                               Set<String> keys) throws IOException {
        InputStream content = azureProvider.getContent(spaceId, contentId);
        Assert.assertNotNull(content);

        String text = IOUtils.toString(content);
        Assert.assertEquals(CONTENT_DATA, text);

        // FIXME: Azure does not provide md5 guarantees.
        // ChecksumUtil cksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        // String cksumFromStore = cksumUtil.generateChecksum(text);
        // Assert.assertEquals(md5, cksumFromStore);

        Map<String, String> propsFromStore = azureProvider.getContentProperties(
            spaceId,
            contentId);
        Assert.assertNotNull(propsFromStore);
        Assert.assertEquals(props.size(), propsFromStore.size());

        for (String key : keys) {
            Assert.assertTrue(propsFromStore.containsKey(key));
            Assert.assertTrue(props.containsKey(key));
            Assert.assertEquals(props.get(key), propsFromStore.get(key));
        }

        log.info("props: {}" + propsFromStore);
    }

}
