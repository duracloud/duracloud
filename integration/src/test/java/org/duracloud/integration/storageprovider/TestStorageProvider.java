/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.storageprovider;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.duracloud.storage.util.StorageProviderUtil.compareChecksum;
import static org.duracloud.storage.util.StorageProviderUtil.contains;
import static org.duracloud.storage.util.StorageProviderUtil.count;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.storage.domain.RetrievedContent;
import org.duracloud.storage.error.ChecksumMismatchException;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProviderBase;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bill Branan
 * Date: 9/8/14
 */
public abstract class TestStorageProvider {

    protected static final Logger log =
        LoggerFactory.getLogger(TestStorageProvider.class);

    protected StorageProvider storageProvider;
    protected final List<String> spaceIds = new ArrayList<>();
    protected String namePrefix = "storageprovider";

    protected static final String CONTENT_PROPS_NAME =
        "custom-content-properties";
    protected static final String CONTENT_PROPS_VALUE = "Testing Content";
    protected static final String CONTENT_MIME_NAME =
        StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
    protected static final String CONTENT_MIME_VALUE = "text/plain";
    protected static final String CONTENT_DATA = "Test Content";

    public abstract StorageProvider createStorageProvider();

    @Before
    public void setup() {
        storageProvider = createStorageProvider();
        Assume.assumeTrue(storageProvider != null);
    }

    @After
    public void tearDown() {
        log.debug("tearDown");
        // delete all the test spaces
        for (String spaceId : spaceIds) {
            try {
                ((StorageProviderBase) storageProvider).deleteSpaceSync(spaceId);
            } catch (Exception e) {
                // do nothing.
            }
        }
        storageProvider = null;
    }

    @Test
    public void testStorageProvider() throws Exception {
        /* Test Spaces */
        String SPACE_ID = getNewSpaceId();

        // test createSpace()
        log.debug("Test createSpace()");
        storageProvider.createSpace(SPACE_ID);

        // test getSpaceProperties()
        log.debug("Test getSpaceProperties()");
        testSpaceProperties(SPACE_ID);

        // test getSpaces()
        log.debug("Test getSpaces()");
        Iterator<String> spaces = storageProvider.getSpaces();
        assertNotNull(spaces);
        // This will only work when SPACE_ID fits the bucket/container
        // naming conventions
        assertTrue(contains(spaces, SPACE_ID));

        /* Test Content */

        // test addContent()
        log.debug("Test addContent()");
        final String CONTENT_ID = getNewContentId();
        addContent(SPACE_ID, CONTENT_ID, CONTENT_MIME_VALUE, false);

        log.debug("Test addContent() with invalid checksum");
        String contentIdInvalidChecksum = getNewContentId();
        boolean exceptionThrown =
            addContentWithInvalidChecksum(SPACE_ID,
                                          contentIdInvalidChecksum,
                                          CONTENT_MIME_VALUE);
        assertTrue(exceptionThrown);

        // test getContentProperties()
        log.debug("Test getContentProperties()");
        Map<String, String> cProperties =
            storageProvider.getContentProperties(SPACE_ID, CONTENT_ID);
        assertNotNull(cProperties);
        assertEquals(CONTENT_MIME_VALUE, cProperties.get(CONTENT_MIME_NAME));
        assertNotNull(cProperties.get(StorageProvider.PROPERTIES_CONTENT_MODIFIED));
        assertNotNull(cProperties.get(StorageProvider.PROPERTIES_CONTENT_SIZE));
        assertNotNull(cProperties.get(StorageProvider.PROPERTIES_CONTENT_CHECKSUM));

        // add additional content for getContents tests
        String testContent2 = "test-content-2";
        addContent(SPACE_ID, testContent2, CONTENT_MIME_VALUE, false);
        String testContent3 = "test-content-3";
        addContent(SPACE_ID, testContent3, null, true);

        // test getSpaceContents()
        log.debug("Test getSpaceContents()");
        Iterator<String> spaceContents =
            storageProvider.getSpaceContents(SPACE_ID, null);
        assertNotNull(spaceContents);
        assertEquals(3, count(spaceContents));

        // test getSpaceContentsChunked() maxLimit
        log.debug("Test getSpaceContentsChunked() maxLimit");
        List<String> spaceContentList =
            storageProvider.getSpaceContentsChunked(SPACE_ID,
                                                    null,
                                                    2,
                                                    null);
        assertNotNull(spaceContentList);
        assertEquals(2, spaceContentList.size());
        String lastItem = spaceContentList.get(spaceContentList.size() - 1);
        spaceContentList = storageProvider.getSpaceContentsChunked(SPACE_ID,
                                                                   null,
                                                                   2,
                                                                   lastItem);
        assertNotNull(spaceContentList);
        assertEquals(1, spaceContentList.size());

        // test getSpaceContentsChunked() prefix
        log.debug("Test getSpaceContentsChunked() prefix");
        spaceContentList = storageProvider.getSpaceContentsChunked(SPACE_ID,
                                                                   "test",
                                                                   10,
                                                                   null);
        assertEquals(2, spaceContentList.size());

        // test getContent()
        log.debug("Test getContent()");
        InputStream is = storageProvider.getContent(SPACE_ID, CONTENT_ID).getContentStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String contentLine = reader.readLine();
        assertTrue(contentLine.equals(CONTENT_DATA));

        // test getContent() range
        log.debug("Test getContent() range");
        RetrievedContent retrievedContent =
            storageProvider.getContent(SPACE_ID, CONTENT_ID, "bytes=0-4");
        is = retrievedContent.getContentStream();
        reader = new BufferedReader(new InputStreamReader(is));
        contentLine = reader.readLine();
        assertFalse(contentLine.equals(CONTENT_DATA));
        assertTrue(CONTENT_DATA.startsWith(contentLine));
        assertEquals(5, contentLine.getBytes().length);
        Map<String, String> rangeMetadata = retrievedContent.getContentProperties();
        assertEquals("5", rangeMetadata.get(HttpHeaders.CONTENT_LENGTH));
        assertEquals("bytes 0-4/12", rangeMetadata.get(HttpHeaders.CONTENT_RANGE));

        // test invalid content
        log.debug("Test getContent() with invalid content ID");
        log.debug("-- Begin expected error log -- ");
        try {
            storageProvider.getContent(SPACE_ID, "non-existant-content");
            fail("Exception expected");
        } catch (Exception e) {
            assertNotNull(e);
        }
        log.debug("-- End expected error log --");

        // test setContentProperties()
        log.debug("Test setContentProperties()");
        Map<String, String> contentProperties = new HashMap<String, String>();
        contentProperties.put(CONTENT_PROPS_NAME, CONTENT_PROPS_VALUE);
        storageProvider.setContentProperties(SPACE_ID,
                                             CONTENT_ID,
                                             contentProperties);

        // test getContentProperties()
        log.debug("Test getContentProperties()");
        cProperties = storageProvider.getContentProperties(SPACE_ID, CONTENT_ID);
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
        storageProvider.setContentProperties(SPACE_ID,
                                             CONTENT_ID,
                                             contentProperties);
        cProperties = storageProvider.getContentProperties(SPACE_ID, CONTENT_ID);
        assertNotNull(cProperties);
        assertEquals(newMime, cProperties.get(CONTENT_MIME_NAME));
        // Custom properties was not included in update, it should be removed
        assertNull(cProperties.get(CONTENT_PROPS_NAME));

        log.debug("Test getContentProperties() - mimetype default");
        cProperties = storageProvider.getContentProperties(SPACE_ID, testContent3);
        assertNotNull(cProperties);
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                     cProperties.get(CONTENT_MIME_NAME));

        /* Test Deletes */

        // test deleteContent()
        log.debug("Test deleteContent()");
        storageProvider.deleteContent(SPACE_ID, CONTENT_ID);
        spaceContents = storageProvider.getSpaceContents(SPACE_ID, null);
        assertFalse(contains(spaceContents, CONTENT_ID));

        // test deleteSpace()
        log.debug("Test deleteSpace()");
        ((StorageProviderBase) storageProvider).deleteSpaceSync(SPACE_ID);
        // remove the space we just deleted from our collection of space IDs
        // because we don't need to try to delete it again in tearDown.
        spaceIds.remove(SPACE_ID);
        spaces = storageProvider.getSpaces();
        assertFalse(contains(spaces, SPACE_ID));
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
            storageProvider.getSpaceProperties(spaceId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            storageProvider.getSpaceContents(spaceId, null);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            storageProvider.getSpaceContentsChunked(spaceId, null, 100, null);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            storageProvider.deleteSpace(spaceId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            int contentSize = content.length;
            ByteArrayInputStream contentStream = new ByteArrayInputStream(content);
            storageProvider.addContent(spaceId,
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
            storageProvider.getContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            storageProvider.getContentProperties(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            storageProvider.setContentProperties(spaceId,
                                                 contentId,
                                                 new HashMap<String, String>());
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            storageProvider.deleteContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        // Content Not Found

        storageProvider.createSpace(spaceId);
        failMsg = "Should throw NotFoundException attempting to " +
                  "access content which does not exist";

        try {
            storageProvider.getContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            storageProvider.getContentProperties(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            storageProvider.setContentProperties(spaceId,
                                                 contentId,
                                                 new HashMap<String, String>());
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            storageProvider.deleteContent(spaceId, contentId);
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

        doTestCopyContent(srcSpaceId, srcContentId, srcSpaceId, srcContentId);
    }

    @Test
    public void testCopyContentSameSpaceDifferentName() throws Exception {
        String srcSpaceId = getNewSpaceId();

        String srcContentId = getNewContentId();
        String destContentId = getNewContentId();

        doTestCopyContent(srcSpaceId, srcContentId, srcSpaceId, destContentId);
    }

    private void addContent(String spaceId,
                            String contentId,
                            String mimeType,
                            boolean checksumInAdvance) {
        byte[] content = CONTENT_DATA.getBytes();
        ByteArrayInputStream contentStream = new ByteArrayInputStream(content);

        String advChecksum = null;
        if (checksumInAdvance) {
            ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
            advChecksum = util.generateChecksum(contentStream);
            contentStream.reset();
        }

        String checksum =
            doAddContent(spaceId, contentId, content.length, contentStream,
                         mimeType, advChecksum);

        if (checksumInAdvance) {
            assertEquals(advChecksum, checksum);
        }
    }

    private String doAddContent(String spaceId,
                                String contentId,
                                int contentSize,
                                InputStream contentStream,
                                String mimeType,
                                String advChecksum) {
        String checksum = storageProvider.addContent(spaceId,
                                                     contentId,
                                                     mimeType,
                                                     null,
                                                     contentSize,
                                                     advChecksum,
                                                     contentStream);

        waitForEventualConsistency(spaceId, contentId);
        compareChecksum(storageProvider, spaceId, contentId, checksum);
        return checksum;
    }

    private boolean addContentWithInvalidChecksum(String spaceId,
                                                  String contentId,
                                                  String mimeType) {
        byte[] content = CONTENT_DATA.getBytes();
        ByteArrayInputStream contentStream = new ByteArrayInputStream(content);

        String invalidChecksum = "abcdefg";

        try {
            doAddContent(spaceId, contentId, content.length, contentStream,
                         mimeType, invalidChecksum);
            fail("ChecksumMismatchException expected based on invalid checksum");
        } catch (ChecksumMismatchException e) {
            try {
                // Verify that the content was either not added, or was deleted
                waitForEventualConsistencyOnDelete(spaceId, contentId);
                storageProvider.getContentProperties(spaceId, contentId);
                fail("On checksum mismatch, content item " +
                     "should have been removed from the space");
            } catch (NotFoundException nfe) {
                return true;
            }
        }
        return false;
    }

    private void waitForEventualConsistency(String spaceId, String contentId) {
        final int maxTries = 10;
        int tries = 0;

        Map<String, String> props = null;
        while (null == props && tries++ < maxTries) {
            try {
                props = storageProvider.getContentProperties(spaceId,
                                                             contentId);
            } catch (Exception e) {
                // do nothing
            }

            if (null == props) {
                log.debug("Waiting for eventual consistency, attempt " + tries);
                sleep(tries * 500);
            }
        }
    }

    private void waitForEventualConsistencyOnDelete(String spaceId,
                                                    String contentId) {
        final int maxTries = 10;
        int tries = 0;

        Map<String, String> props = null;
        do {
            try {
                props = storageProvider.getContentProperties(spaceId,
                                                             contentId);
            } catch (Exception e) {
                // do nothing
            }

            if (null != props) {
                log.debug("Waiting for eventual consistency on delete, " +
                          "attempt " + tries);
                sleep(tries * 500);
            }
        } while (null != props && tries++ < maxTries);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    private Map<String, String> testSpaceProperties(String spaceId) {
        Map<String, String> sProperties =
            storageProvider.getSpaceProperties(spaceId);

        assertTrue(sProperties.containsKey(
            StorageProvider.PROPERTIES_SPACE_CREATED));
        assertNotNull(sProperties.get(StorageProvider.PROPERTIES_SPACE_CREATED));

        assertTrue(sProperties.containsKey(StorageProvider.PROPERTIES_SPACE_COUNT));
        assertNotNull(sProperties.get(StorageProvider.PROPERTIES_SPACE_COUNT));

        return sProperties;
    }

    private void doTestCopyContent(String srcSpaceId,
                                   String srcContentId,
                                   String destSpaceId,
                                   String destContentId) throws Exception {
        this.storageProvider.createSpace(srcSpaceId);
        if (!srcSpaceId.equals(destSpaceId)) {
            this.storageProvider.createSpace(destSpaceId);
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

        storageProvider.setContentProperties(srcSpaceId,
                                             srcContentId,
                                             userProps);
        Map<String, String> props = storageProvider.getContentProperties(
            srcSpaceId,
            srcContentId);
        verifyContent(srcSpaceId,
                      srcContentId,
                      cksum,
                      props,
                      userProps.keySet());

        String md5 = storageProvider.copyContent(srcSpaceId,
                                                 srcContentId,
                                                 destSpaceId,
                                                 destContentId);
        Assert.assertNotNull(md5);
        Assert.assertEquals(cksum, md5);

        verifyContent(destSpaceId,
                      destContentId,
                      md5,
                      props,
                      userProps.keySet());
    }

    private void verifyContent(String spaceId,
                               String contentId,
                               String md5,
                               Map<String, String> props,
                               Set<String> keys) throws IOException {
        InputStream content = storageProvider.getContent(spaceId, contentId).getContentStream();
        Assert.assertNotNull(content);

        String text = IOUtils.toString(content);
        Assert.assertEquals(CONTENT_DATA, text);

        ChecksumUtil cksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String cksumFromStore = cksumUtil.generateChecksum(text);
        Assert.assertEquals(md5, cksumFromStore);

        Map<String, String> propsFromStore =
            storageProvider.getContentProperties(spaceId, contentId);
        Assert.assertNotNull(propsFromStore);
        Assert.assertEquals(props.size(), propsFromStore.size());

        for (String key : keys) {
            Assert.assertTrue(propsFromStore.containsKey(key));
            Assert.assertTrue(props.containsKey(key));
            Assert.assertEquals(props.get(key), propsFromStore.get(key));
        }

        log.info("props: " + propsFromStore);
    }

    private String getNewSpaceId() {
        String random = String.valueOf(new Random().nextInt(99999));
        String spaceId = namePrefix + "-test-space-" + random;
        spaceIds.add(spaceId);
        return spaceId;
    }

    private String getNewContentId() {
        String random = String.valueOf(new Random().nextInt(99999));
        String contentId = namePrefix + "-test-content-" + random;
        return contentId;
    }

}
