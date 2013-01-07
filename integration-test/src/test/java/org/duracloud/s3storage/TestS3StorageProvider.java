/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import com.amazonaws.services.s3.Headers;
import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.provider.StorageProvider;
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
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.duracloud.storage.util.StorageProviderUtil.compareChecksum;
import static org.duracloud.storage.util.StorageProviderUtil.contains;
import static org.duracloud.storage.util.StorageProviderUtil.count;

/**
 * Tests the S3 Storage Provider. This test is run via the command line in order
 * to allow passing in S3 credentials.
 *
 * @author Bill Branan
 */
public class TestS3StorageProvider extends S3ProviderTestBase {

    protected static final Logger log = LoggerFactory.getLogger(
        TestS3StorageProvider.class);

    private S3StorageProvider s3Provider;
    private final List<String> spaceIds = new ArrayList<String>();

    private static final String SPACE_PROPS_NAME = "custom-space-properties";
    private static final String SPACE_PROPS_VALUE = "Testing Space";
    private static final String CONTENT_PROPS_NAME = "custom-content-properties";
    private static final String CONTENT_PROPS_VALUE = "Testing Content";
    private static final String CONTENT_MIME_NAME =
        StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
    private static final String CONTENT_MIME_VALUE = "text/plain";
    private static final String CONTENT_DATA = "Test Content";

    private RestHttpHelper restHelper;

    @Before
    public void setUp() throws Exception {
        Credential s3Credential = getCredential();
        s3Provider = new S3StorageProvider(s3Credential.getUsername(), 
                                           s3Credential.getPassword());
        restHelper = new RestHttpHelper();
    }

    @After
    public void tearDown() {
        clean();
        s3Provider = null;
    }

    private void clean() {
        for (String spaceId : spaceIds) {
            try {
                s3Provider.deleteSpace(spaceId);
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

    @Test
    public void testS3StorageProvider() throws Exception {
        String spaceId = getNewSpaceId();

        /* Test Spaces */

        // test createSpace()
        log.debug("Test createSpace()");
        s3Provider.createSpace(spaceId);
        testSpaceProperties(spaceId);

        // test getSpaceProperties()
        log.debug("Test getSpaceProperties()");
        Map<String, String> sProperties = testSpaceProperties(spaceId);

        // test getSpaces()
        log.debug("Test getSpaces()");
        Iterator<String> spaces = s3Provider.getSpaces();
        assertNotNull(spaces);
        // This will only work when spaceId fits the S3 bucket naming conventions
        assertTrue(contains(spaces, spaceId)); 

        // Check S3 bucket access
        log.debug("Check S3 bucket access");
        String bucketName = s3Provider.getBucketName(spaceId);
        String spaceUrl = "http://" + bucketName + ".s3.amazonaws.com";

        HttpResponse spaceResponse = restHelper.get(spaceUrl);
        // Expect a 403 forbidden error because bucket access is always restricted
        assertEquals(HttpStatus.SC_FORBIDDEN, spaceResponse.getStatusCode());

        /* Test Content */

        // test addContent()
        log.debug("Test addContent()");
        String contentId = getNewContentId();
        addContent(spaceId, contentId, CONTENT_MIME_VALUE, false);

        // test getContentProperties()
        log.debug("Test getContentProperties()");
        Map<String, String> cProperties = s3Provider.getContentProperties(spaceId,
                                                                          contentId);
        assertNotNull(cProperties);
        assertEquals(CONTENT_MIME_VALUE, cProperties.get(CONTENT_MIME_NAME));
        assertEquals(CONTENT_MIME_VALUE,
                     cProperties.get(Headers.CONTENT_TYPE));
        assertNotNull(cProperties.get(StorageProvider.PROPERTIES_CONTENT_SIZE));
        assertNotNull(cProperties.get(StorageProvider.PROPERTIES_CONTENT_CHECKSUM));
        // Make sure date is in RFC-822 format
        String lastModified = cProperties.get(StorageProvider.PROPERTIES_CONTENT_MODIFIED);
        StorageProvider.RFC822_DATE_FORMAT.parse(lastModified);

        // Check content access
        log.debug("Check content access");
        HttpResponse contentResponse =
            restHelper.get(spaceUrl + "/" + contentId);
        // Expect a 403 forbidden error because content access is always restricted
        assertEquals(HttpStatus.SC_FORBIDDEN, contentResponse.getStatusCode());

        // add additional content for getContents tests
        String testContent2 = "test-content-2";
        addContent(spaceId, testContent2, CONTENT_MIME_VALUE, false);
        String testContent3 = "test-content-3";
        addContent(spaceId, testContent3, null, true);

        // test getSpaceContents()
        log.debug("Test getSpaceContents()");
        Iterator<String> spaceContents =
            s3Provider.getSpaceContents(spaceId, null);
        assertNotNull(spaceContents);
        assertEquals(3, count(spaceContents));
        // Ensure that space properties is not included in contents list
        spaceContents = s3Provider.getSpaceContents(spaceId, null);
        String spaceMetaSuffix = S3StorageProvider.SPACE_PROPERTIES_SUFFIX;
        assertFalse(contains(spaceContents, bucketName + spaceMetaSuffix));

        // test getSpaceContentsChunked() maxLimit
        log.debug("Test getSpaceContentsChunked() maxLimit");
        List<String> spaceContentList =
            s3Provider.getSpaceContentsChunked(spaceId, null, 2, null);
        assertNotNull(spaceContentList);
        assertEquals(2, spaceContentList.size());
        String lastItem = spaceContentList.get(spaceContentList.size()-1);
        spaceContentList =
            s3Provider.getSpaceContentsChunked(spaceId, null, 2, lastItem);
        assertNotNull(spaceContentList);
        assertEquals(1, spaceContentList.size());

        // test getSpaceContentsChunked() prefix
        log.debug("Test getSpaceContentsChunked() prefix");
        spaceContentList =
            s3Provider.getSpaceContentsChunked(spaceId, "test", 10, null);
        assertEquals(2, spaceContentList.size());

        // test getContent()
        log.debug("Test getContent()");
        InputStream is = s3Provider.getContent(spaceId, contentId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String contentLine = reader.readLine();
        assertTrue(contentLine.equals(CONTENT_DATA));

        // test invalid content
        log.debug("Test getContent() with invalid content ID");
        log.debug("-- Begin expected error log -- ");
        try {
            is = s3Provider.getContent(spaceId, "non-existant-content");
            fail("Exception expected");
        } catch (Exception e) {
        }
        log.debug("-- End expected error log --");

        // test setContentProperties()
        log.debug("Test setContentProperties()");
        Map<String, String> contentProperties = new HashMap<String, String>();
        contentProperties.put(CONTENT_PROPS_NAME, CONTENT_PROPS_VALUE);
        s3Provider.setContentProperties(spaceId, contentId, contentProperties);

        // test getContentProperties()
        log.debug("Test getContentProperties()");
        cProperties = s3Provider.getContentProperties(spaceId, contentId);
        assertNotNull(cProperties);
        assertEquals(CONTENT_PROPS_VALUE, cProperties.get(CONTENT_PROPS_NAME));
        // Mime type was not included when setting content properties
        // so its value should have been maintained
        assertEquals(CONTENT_MIME_VALUE, cProperties.get(CONTENT_MIME_NAME));
        assertEquals(CONTENT_MIME_VALUE,
                     cProperties.get(Headers.CONTENT_TYPE));

        // test setContentProperties() - mimetype
        log.debug("Test setContentProperties() - mimetype");
        contentProperties = new HashMap<String, String>();
        contentProperties.put(CONTENT_MIME_NAME,
                            StorageProvider.DEFAULT_MIMETYPE);
        s3Provider.setContentProperties(spaceId, contentId, contentProperties);
        cProperties = s3Provider.getContentProperties(spaceId, contentId);
        assertNotNull(cProperties);
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                     cProperties.get(CONTENT_MIME_NAME));
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                     cProperties.get(Headers.CONTENT_TYPE));

        log.debug("Test getContentProperties() - mimetype default");
        cProperties = s3Provider.getContentProperties(spaceId, testContent3);
        assertNotNull(cProperties);
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                     cProperties.get(CONTENT_MIME_NAME));
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                     cProperties.get(Headers.CONTENT_TYPE));

        /* Test Deletes */

        // test deleteContent()
        log.debug("Test deleteContent()");
        s3Provider.deleteContent(spaceId, contentId);
        spaceContents = s3Provider.getSpaceContents(spaceId, null);
        assertFalse(contains(spaceContents, contentId));

        // test deleteSpace()
        log.debug("Test deleteSpace()");
        s3Provider.deleteSpace(spaceId);
        spaces = s3Provider.getSpaces();
        assertFalse(contains(spaces, spaceId));
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

        String checksum = s3Provider.addContent(spaceId,
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

        compareChecksum(s3Provider, spaceId, contentId, checksum);
    }

    private void waitForEventualConsistency(String spaceId, String contentId) {
        final int maxTries = 10;
        int tries = 0;

        Map<String, String> props = null;
        while (null == props && tries++ < maxTries) {
            try {
                props = s3Provider.getContentProperties(spaceId, contentId);
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
        Map<String, String> sProperties = s3Provider.getSpaceProperties(spaceId);

        assertTrue(sProperties.containsKey(
            StorageProvider.PROPERTIES_SPACE_CREATED));
        assertNotNull(sProperties.get(StorageProvider.PROPERTIES_SPACE_CREATED));

        assertTrue(sProperties.containsKey(StorageProvider.PROPERTIES_SPACE_COUNT));
        assertNotNull(sProperties.get(StorageProvider.PROPERTIES_SPACE_COUNT));

        return sProperties;
    }

    @Test
    public void testNotFound() {
        String spaceId = "NonExistantSpace";
        String contentId = "NonExistantContent";
        String failMsg = "Should throw NotFoundException attempting to " +
                         "access a space which does not exist";
        byte[] content = CONTENT_DATA.getBytes();

        // Space Not Found

        try {
            s3Provider.getSpaceProperties(spaceId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.getSpaceContents(spaceId, null);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.getSpaceContentsChunked(spaceId, null, 100, null);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.deleteSpace(spaceId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            int contentSize = content.length;
            ByteArrayInputStream contentStream = new ByteArrayInputStream(
                content);
            s3Provider.addContent(spaceId,
                                  contentId,
                                  CONTENT_MIME_VALUE,
                                  null,
                                  contentSize,
                                  null,
                                  contentStream);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.getContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.getContentProperties(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.setContentProperties(spaceId,
                                            contentId,
                                            new HashMap<String, String>());
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.deleteContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        // Content Not Found

        spaceId = getNewSpaceId();
        s3Provider.createSpace(spaceId);
        failMsg = "Should throw NotFoundException attempting to " +
                  "access content which does not exist";        

        try {
            s3Provider.getContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.getContentProperties(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.setContentProperties(spaceId,
                                            contentId,
                                            new HashMap<String, String>());
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.deleteContent(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void testSetContentProperties() {
        // StorageProvider properties which is expected to be available
        String name00 = StorageProvider.PROPERTIES_CONTENT_CHECKSUM;
        String value00 = "c56f855f5dec9276733ff3e2c66ec7df";
        String name01 = StorageProvider.PROPERTIES_CONTENT_MD5;
        String value01 = "c56f855f5dec9276733ff3e2c66ec7df";
        String name02 = StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
        String value02 = "text/html";
        String name03 = StorageProvider.PROPERTIES_CONTENT_SIZE;
        String value03 = "59142";
        String name04 = StorageProvider.PROPERTIES_CONTENT_MODIFIED;
        String value04 = "Wed, 25 Nov 2009 11:09:39 EST";

        // Custom properties values
        String name05 = "request-id";
        String value05 = "BA66CF8BF16D69CE";
        String name06 = "id-2";
        String value06 = "GssZwY9fOk4ncr6CZneS1ndkLkfLD7y2vfK12O6PjjeFkQV56Z40WU7eOIPmjdaA";
        String name07 = "tags";
        String value07 = "a1";
        String name08 = "Server";
        String value08 = "Apache-Coyote/1.1";

        // Http header values which are expected to be available
        String name09 = "ETag";
        String value09 = "\"c56f855f5dec9276733ff3e2c66ec7df\"";
        String name10 = "Content-Type";
        String value10 = "text/html";
        String name11 = "Content-Length";
        String value11 = "59142";
        String name12 = "Last-Modified";
        String value12 = "Wed, 25 Nov 2009 11:09:39 EST";

        // Http header values which are not expected to be available
        String name13 = "Date";
        String value13 = "Wed, 25 Nov 2009 16:47:07 GMT";

        Map<String, String> contentProperties = new HashMap<String, String>();
        contentProperties.put(name00, value00);
        contentProperties.put(name01, value01);
        contentProperties.put(name02, value02);
        contentProperties.put(name03, value03);
        contentProperties.put(name04, value04);
        contentProperties.put(name05, value05);
        contentProperties.put(name06, value06);
        contentProperties.put(name07, value07);
        contentProperties.put(name08, value08);
        contentProperties.put(name09, value09);
        contentProperties.put(name10, value10);
        contentProperties.put(name11, value11);
        contentProperties.put(name12, value12);
        contentProperties.put(name13, value13);

        // Set up the space and content
        String spaceId = getNewSpaceId();
        String contentId = getNewContentId();

        s3Provider.createSpace(spaceId);
        addContent(spaceId, contentId, CONTENT_MIME_VALUE, false);

        // This is the method under test.
        s3Provider.setContentProperties(spaceId, contentId, contentProperties);

        Map<String, String> properties = s3Provider.getContentProperties(spaceId,
                                                                         contentId);
        Assert.assertNotNull(properties);

        Assert.assertTrue(properties.containsKey(name00));
        Assert.assertTrue(properties.containsKey(name01));
        Assert.assertTrue(properties.containsKey(name02));
        Assert.assertTrue(properties.containsKey(name03));
        Assert.assertTrue(properties.containsKey(name04));
        Assert.assertTrue(properties.containsKey(name05));
        Assert.assertTrue(properties.containsKey(name06));
        Assert.assertTrue(properties.containsKey(name07));
        Assert.assertTrue(properties.containsKey(name08.toLowerCase()));
        Assert.assertTrue(properties.containsKey(name09));
        Assert.assertTrue(properties.containsKey(name10));
        Assert.assertTrue(properties.containsKey(name11));
        Assert.assertTrue(properties.containsKey(name12));
        Assert.assertFalse(properties.containsKey(name13));

        Assert.assertNotNull(properties.get(name00));
        Assert.assertNotNull(properties.get(name01));
        Assert.assertNotNull(properties.get(name02));
        Assert.assertNotNull(properties.get(name03));
        Assert.assertNotNull(properties.get(name04));
        Assert.assertNotNull(properties.get(name05));
        Assert.assertNotNull(properties.get(name06));
        Assert.assertNotNull(properties.get(name07));
        Assert.assertNotNull(properties.get(name08.toLowerCase()));
        Assert.assertNotNull(properties.get(name09));
        Assert.assertNotNull(properties.get(name10));
        Assert.assertNotNull(properties.get(name11));
        Assert.assertNotNull(properties.get(name12));
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
        this.s3Provider.createSpace(srcSpaceId);
        if (!srcSpaceId.equals(destSpaceId)) {
            this.s3Provider.createSpace(destSpaceId);
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

        s3Provider.setContentProperties(srcSpaceId, srcContentId, userProps);
        Map<String, String> props = s3Provider.getContentProperties(srcSpaceId,
                                                                    srcContentId);
        verifyContent(srcSpaceId,
                      srcContentId,
                      cksum,
                      props,
                      userProps.keySet());

        String md5 = s3Provider.copyContent(srcSpaceId,
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
        InputStream content = s3Provider.getContent(spaceId, contentId);
        Assert.assertNotNull(content);

        String text = IOUtils.toString(content);
        Assert.assertEquals(CONTENT_DATA, text);

        ChecksumUtil cksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String cksumFromStore = cksumUtil.generateChecksum(text);
        Assert.assertEquals(md5, cksumFromStore);

        Map<String, String> propsFromStore = s3Provider.getContentProperties(
            spaceId,
            contentId);
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
