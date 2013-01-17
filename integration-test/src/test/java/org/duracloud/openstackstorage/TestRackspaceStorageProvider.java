/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.openstackstorage;

import com.rackspacecloud.client.cloudfiles.FilesClient;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.rackspacestorage.RackspaceStorageProvider;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.NotFoundException;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.duracloud.storage.util.StorageProviderUtil.compareChecksum;
import static org.duracloud.storage.util.StorageProviderUtil.contains;
import static org.duracloud.storage.util.StorageProviderUtil.count;

/**
 * Tests the Rackspace Storage Provider. This test is run via the command line
 * in order to allow passing in credentials.
 *
 * @author Bill Branan
 */
public class TestRackspaceStorageProvider {

    protected static final Logger log =
            LoggerFactory.getLogger(TestRackspaceStorageProvider.class);

    OpenStackStorageProvider rackspaceProvider;
    private final List<String> spaceIds = new ArrayList<String>();

    FilesClient filesClient;
    
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
        Credential rackspaceCredential = getCredential();
        Assert.assertNotNull(rackspaceCredential);

        String username = rackspaceCredential.getUsername();
        String password = rackspaceCredential.getPassword();
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);

        rackspaceProvider = new RackspaceStorageProvider(username, password);
        filesClient = new FilesClient(username, password);
        assertTrue(filesClient.login());
    }

    @After
    public void tearDown() {
        clean();
        rackspaceProvider = null;
        filesClient = null;
    }

    private void clean() {
        for (String spaceId : spaceIds) {
            try {
                rackspaceProvider.deleteSpace(spaceId);
            } catch (Exception e) {
                // do nothing.
            }
        }
    }

    private Credential getCredential() throws Exception {
        UnitTestDatabaseUtil dbUtil = new UnitTestDatabaseUtil();
        return dbUtil.findCredentialForResource(ResourceType.fromStorageProviderType(
                                                StorageProviderType.RACKSPACE));
    }

    private String getNewSpaceId() {
        String random = String.valueOf(new Random().nextInt(99999));
        String spaceId = "rackstore-test-space-" + random;
        spaceIds.add(spaceId);
        return spaceId;
    }

    private String getNewContentId() {
        String random = String.valueOf(new Random().nextInt(99999));
        String contentId = "rackstore-test-content-" + random;
        return contentId;
    }

    @Test
    public void testRackspaceStorageProvider() throws Exception {
        /* Test Spaces */
        String SPACE_ID = getNewSpaceId();

        // test createSpace()
        log.debug("Test createSpace()");
        rackspaceProvider.createSpace(SPACE_ID);
        testSpaceProperties(SPACE_ID);

        // test getSpaceProperties()
        log.debug("Test getSpaceProperties()");
        Map<String, String> sProperties = testSpaceProperties(SPACE_ID);

        // test getSpaces()
        log.debug("Test getSpaces()");
        Iterator<String> spaces = rackspaceProvider.getSpaces();
        assertNotNull(spaces);
        // This will only work when SPACE_ID fits the Rackspace container naming conventions
        assertTrue(contains(spaces, SPACE_ID));

        // Check Rackspace CDN access - should be off (as always)
        log.debug("Check space access");
        assertFalse(filesClient.isCDNEnabled(SPACE_ID));

        /* Test Content */

        // test addContent()
        log.debug("Test addContent()");
        String CONTENT_ID = getNewContentId();
        addContent(SPACE_ID, CONTENT_ID, CONTENT_MIME_VALUE, false);

        // test getContentProperties()
        log.debug("Test getContentProperties()");
        Map<String, String> cProperties =
                rackspaceProvider.getContentProperties(SPACE_ID, CONTENT_ID);
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
            rackspaceProvider.getSpaceContents(SPACE_ID, null);
        assertNotNull(spaceContents);
        assertEquals(3, count(spaceContents));

        // test getSpaceContentsChunked() maxLimit
        log.debug("Test getSpaceContentsChunked() maxLimit");
        List<String> spaceContentList =
            rackspaceProvider.getSpaceContentsChunked(SPACE_ID,
                                                      null,
                                                      2,
                                                      null);
        assertNotNull(spaceContentList);
        assertEquals(2, spaceContentList.size());
        String lastItem = spaceContentList.get(spaceContentList.size() - 1);
        spaceContentList = rackspaceProvider.getSpaceContentsChunked(SPACE_ID,
                                                                     null,
                                                                     2,
                                                                     lastItem);
        assertNotNull(spaceContentList);
        assertEquals(1, spaceContentList.size());

        // test getSpaceContentsChunked() prefix
        log.debug("Test getSpaceContentsChunked() prefix");
        spaceContentList = rackspaceProvider.getSpaceContentsChunked(SPACE_ID,
                                                                     "test",
                                                                     10,
                                                                     null);
        assertEquals(2, spaceContentList.size());

        // test getContent()
        log.debug("Test getContent()");
        InputStream is = rackspaceProvider.getContent(SPACE_ID, CONTENT_ID);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String contentLine = reader.readLine();
        assertTrue(contentLine.equals(CONTENT_DATA));

        // test invalid content
        log.debug("Test getContent() with invalid content ID");       
        log.debug("-- Begin expected error log -- ");
        try {
            rackspaceProvider.getContent(SPACE_ID, "non-existant-content");
            fail("Exception expected");
        } catch (Exception e) {
            assertNotNull(e);
        }
        log.debug("-- End expected error log --");

        // test setContentProperties()
        log.debug("Test setContentProperties()");
        Map<String, String> contentProperties = new HashMap<String, String>();
        contentProperties.put(CONTENT_PROPS_NAME, CONTENT_PROPS_VALUE);
        rackspaceProvider.setContentProperties(SPACE_ID,
                                               CONTENT_ID,
                                               contentProperties);

        // test getContentProperties()
        log.debug("Test getContentProperties()");
        cProperties = rackspaceProvider.getContentProperties(SPACE_ID, CONTENT_ID);
        assertNotNull(cProperties);
        assertEquals(CONTENT_PROPS_VALUE, cProperties.get(CONTENT_PROPS_NAME));
        // Mime type was not included when setting content properties
        // so its value should have been maintained
        String mime = CONTENT_MIME_VALUE + "; charset=UTF-8";
        assertEquals(mime, cProperties.get(CONTENT_MIME_NAME));

        // test setContentProperties() - mimetype
        log.debug("Test setContentProperties() - mimetype");
        String newMime = "image/bmp";
        contentProperties = new HashMap<String, String>();
        contentProperties.put(CONTENT_MIME_NAME, newMime);
        rackspaceProvider.setContentProperties(SPACE_ID,
                                               CONTENT_ID,
                                               contentProperties);
        cProperties = rackspaceProvider.getContentProperties(SPACE_ID, CONTENT_ID);
        assertNotNull(cProperties);
        assertEquals(newMime, cProperties.get(CONTENT_MIME_NAME));
        // Custom properties was not included in update, it should be removed
        assertNull(cProperties.get(CONTENT_PROPS_NAME));

        log.debug("Test getContentProperties() - mimetype default");
        cProperties = rackspaceProvider.getContentProperties(SPACE_ID, testContent3);
        assertNotNull(cProperties);
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                     cProperties.get(CONTENT_MIME_NAME));     

        /* Test Deletes */

        // test deleteContent()
        log.debug("Test deleteContent()");
        rackspaceProvider.deleteContent(SPACE_ID, CONTENT_ID);
        spaceContents = rackspaceProvider.getSpaceContents(SPACE_ID, null);
        assertFalse(contains(spaceContents, CONTENT_ID));

        // test deleteSpace()
        log.debug("Test deleteSpace()");
        rackspaceProvider.deleteSpace(SPACE_ID);
        spaces = rackspaceProvider.getSpaces();
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
        if(checksumInAdvance) {
            ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
            advChecksum = util.generateChecksum(contentStream);
            contentStream.reset();
        }

        String checksum = rackspaceProvider.addContent(spaceId,
                                                       contentId,
                                                       mimeType,
                                                        null,
                                                       contentSize,
                                                       advChecksum,
                                                       contentStream);

        if(checksumInAdvance) {
            assertEquals(advChecksum, checksum);
        }

        waitForEventualConsistency(spaceId, contentId);

        compareChecksum(rackspaceProvider, spaceId, contentId, checksum);
    }

    private void waitForEventualConsistency(String spaceId, String contentId) {
        final int maxTries = 10;
        int tries = 0;

        Map<String, String> props = null;
        while (null == props && tries++ < maxTries) {
            try {
                props = rackspaceProvider.getContentProperties(spaceId,
                                                               contentId);
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

    private Map<String, String> testSpaceProperties(String spaceId) {
        Map<String, String> sProperties =
            rackspaceProvider.getSpaceProperties(spaceId);

        assertTrue(sProperties.containsKey(
            StorageProvider.PROPERTIES_SPACE_CREATED));
        assertNotNull(sProperties.get(StorageProvider.PROPERTIES_SPACE_CREATED));

        assertTrue(sProperties.containsKey(StorageProvider.PROPERTIES_SPACE_COUNT));
        assertNotNull(sProperties.get(StorageProvider.PROPERTIES_SPACE_COUNT));

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
            rackspaceProvider.getSpaceProperties(spaceId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            rackspaceProvider.getSpaceContents(spaceId, null);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            rackspaceProvider.getSpaceContentsChunked(spaceId, null, 100, null);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            rackspaceProvider.deleteSpace(spaceId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            int contentSize = content.length;
            ByteArrayInputStream contentStream = new ByteArrayInputStream(
                content);
            rackspaceProvider.addContent(spaceId,
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
            rackspaceProvider.getContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            rackspaceProvider.getContentProperties(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            rackspaceProvider.setContentProperties(spaceId,
                                                   contentId,
                                                   new HashMap<String, String>());
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            rackspaceProvider.deleteContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        // Content Not Found

        rackspaceProvider.createSpace(spaceId);
        failMsg = "Should throw NotFoundException attempting to " +
                  "access content which does not exist";

        try {
            rackspaceProvider.getContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            rackspaceProvider.getContentProperties(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            rackspaceProvider.setContentProperties(spaceId,
                                                   contentId,
                                                   new HashMap<String, String>());
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            rackspaceProvider.deleteContent(spaceId, contentId);
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

    private void doTestCopyContent(String srcSpaceId,
                                   String srcContentId,
                                   String destSpaceId,
                                   String destContentId) throws Exception {
        this.rackspaceProvider.createSpace(srcSpaceId);
        if (!srcSpaceId.equals(destSpaceId)) {
            this.rackspaceProvider.createSpace(destSpaceId);
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

        rackspaceProvider.setContentProperties(srcSpaceId,
                                               srcContentId,
                                               userProps);
        Map<String, String> props = rackspaceProvider.getContentProperties(
            srcSpaceId,
            srcContentId);
        verifyContent(srcSpaceId,
                      srcContentId,
                      cksum,
                      props,
                      userProps.keySet());

        String md5 = rackspaceProvider.copyContent(srcSpaceId,
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
        InputStream content = rackspaceProvider.getContent(spaceId, contentId);
        Assert.assertNotNull(content);

        String text = IOUtils.toString(content);
        Assert.assertEquals(CONTENT_DATA, text);

        ChecksumUtil cksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String cksumFromStore = cksumUtil.generateChecksum(text);
        Assert.assertEquals(md5, cksumFromStore);

        Map<String, String> propsFromStore =
            rackspaceProvider.getContentProperties(spaceId, contentId);
        Assert.assertNotNull(propsFromStore);
        Assert.assertEquals(props.size(), propsFromStore.size());

        for (String key : keys) {
            Assert.assertTrue(propsFromStore.containsKey(key));
            Assert.assertTrue(props.containsKey(key));
            Assert.assertEquals(props.get(key), propsFromStore.get(key));
        }

        log.info("props: " + propsFromStore);
    }

}
