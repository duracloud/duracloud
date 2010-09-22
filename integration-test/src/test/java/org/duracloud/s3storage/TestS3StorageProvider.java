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
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProvider.AccessType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    private static final String SPACE_META_NAME = "custom-space-metadata";
    private static final String SPACE_META_VALUE = "Testing Space";
    private static final String CONTENT_META_NAME = "custom-content-metadata";
    private static final String CONTENT_META_VALUE = "Testing Content";
    private static final String CONTENT_MIME_NAME = StorageProvider.METADATA_CONTENT_MIMETYPE;
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
        testSpaceMetadata(spaceId, AccessType.CLOSED);

        // test setSpaceMetadata()
        log.debug("Test setSpaceMetadata()");
        Map<String, String> spaceMetadata = new HashMap<String, String>();
        spaceMetadata.put(SPACE_META_NAME, SPACE_META_VALUE);
        s3Provider.setSpaceMetadata(spaceId, spaceMetadata);

        // test getSpaceMetadata()
        log.debug("Test getSpaceMetadata()");
         Map<String, String> sMetadata =
             testSpaceMetadata(spaceId, AccessType.CLOSED);
        assertTrue(sMetadata.containsKey(SPACE_META_NAME));
        assertEquals(SPACE_META_VALUE, sMetadata.get(SPACE_META_NAME));

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

        // test setSpaceAccess()
        log.debug("Test setSpaceAccess(OPEN)");
        s3Provider.setSpaceAccess(spaceId, AccessType.OPEN);

        // test getSpaceAccess()
        log.debug("Test getSpaceAccess()");
        AccessType access = s3Provider.getSpaceAccess(spaceId);
        assertEquals(access, AccessType.OPEN);

        // Check space access
        log.debug("Check S3 bucket access");
        spaceResponse = restHelper.get(spaceUrl);
        // Expect a 403 forbidden error because bucket access is always restricted
        assertEquals(HttpStatus.SC_FORBIDDEN, spaceResponse.getStatusCode());

        // test set space access via metadata update
        log.debug("Test setSpaceMetadata(Access) ");
        spaceMetadata = new HashMap<String, String>();
        spaceMetadata.put(StorageProvider.METADATA_SPACE_ACCESS,
                          AccessType.CLOSED.name());
        s3Provider.setSpaceMetadata(spaceId, spaceMetadata);

        // test getSpaceAccess()
        log.debug("Test getSpaceAccess()");
        access = s3Provider.getSpaceAccess(spaceId);
        assertEquals(access, AccessType.CLOSED);

        /* Test Content */

        // test addContent()
        log.debug("Test addContent()");
        String contentId = getNewContentId();
        addContent(spaceId, contentId, CONTENT_MIME_VALUE, false);

        // test getContentMetadata()
        log.debug("Test getContentMetadata()");
        Map<String, String> cMetadata = s3Provider.getContentMetadata(spaceId,
                                                                      contentId);
        assertNotNull(cMetadata);
        assertEquals(CONTENT_MIME_VALUE, cMetadata.get(CONTENT_MIME_NAME));
        assertEquals(CONTENT_MIME_VALUE,
                     cMetadata.get(Headers.CONTENT_TYPE));
        assertNotNull(cMetadata.get(StorageProvider.METADATA_CONTENT_SIZE));
        assertNotNull(cMetadata.get(StorageProvider.METADATA_CONTENT_CHECKSUM));
        // Make sure date is in RFC-822 format
        String lastModified = cMetadata.get(StorageProvider.METADATA_CONTENT_MODIFIED);
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
        // Ensure that space metadata is not included in contents list
        spaceContents = s3Provider.getSpaceContents(spaceId, null);
        String spaceMetaSuffix = S3StorageProvider.SPACE_METADATA_SUFFIX;
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

        // test setContentMetadata()
        log.debug("Test setContentMetadata()");
        Map<String, String> contentMetadata = new HashMap<String, String>();
        contentMetadata.put(CONTENT_META_NAME, CONTENT_META_VALUE);
        s3Provider.setContentMetadata(spaceId, contentId, contentMetadata);

        // test getContentMetadata()
        log.debug("Test getContentMetadata()");
        cMetadata = s3Provider.getContentMetadata(spaceId, contentId);
        assertNotNull(cMetadata);
        assertEquals(CONTENT_META_VALUE, cMetadata.get(CONTENT_META_NAME));
        // Mime type was not included when setting content metadata
        // so its value should have been maintained
        assertEquals(CONTENT_MIME_VALUE, cMetadata.get(CONTENT_MIME_NAME));
        assertEquals(CONTENT_MIME_VALUE,
                     cMetadata.get(Headers.CONTENT_TYPE));

        // test setContentMetadata() - mimetype
        log.debug("Test setContentMetadata() - mimetype");
        contentMetadata = new HashMap<String, String>();
        contentMetadata.put(CONTENT_MIME_NAME,
                            StorageProvider.DEFAULT_MIMETYPE);
        s3Provider.setContentMetadata(spaceId, contentId, contentMetadata);
        cMetadata = s3Provider.getContentMetadata(spaceId, contentId);
        assertNotNull(cMetadata);
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                     cMetadata.get(CONTENT_MIME_NAME));
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                     cMetadata.get(Headers.CONTENT_TYPE));

        log.debug("Test getContentMetadata() - mimetype default");
        cMetadata = s3Provider.getContentMetadata(spaceId, testContent3);
        assertNotNull(cMetadata);
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                     cMetadata.get(CONTENT_MIME_NAME));
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                     cMetadata.get(Headers.CONTENT_TYPE));

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
                                                contentSize,
                                                advChecksum,
                                                contentStream);

        if(checksumInAdvance) {
            assertEquals(advChecksum, checksum);
        }

        compareChecksum(s3Provider, spaceId, contentId, checksum);
    }

    private Map<String, String> testSpaceMetadata(String spaceId,
                                                 AccessType access) {
        Map<String, String> sMetadata = s3Provider.getSpaceMetadata(spaceId);

        assertTrue(sMetadata.containsKey(
            StorageProvider.METADATA_SPACE_CREATED));
        assertNotNull(sMetadata.get(StorageProvider.METADATA_SPACE_CREATED));

        assertTrue(sMetadata.containsKey(
            StorageProvider.METADATA_SPACE_COUNT));
        assertNotNull(sMetadata.get(StorageProvider.METADATA_SPACE_COUNT));

        assertTrue(sMetadata.containsKey(
            StorageProvider.METADATA_SPACE_ACCESS));
        String spaceAccess =
            sMetadata.get(StorageProvider.METADATA_SPACE_ACCESS);
        assertNotNull(spaceAccess);
        assertEquals(access.name(), spaceAccess);

        return sMetadata;
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
            s3Provider.getSpaceMetadata(spaceId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.setSpaceMetadata(spaceId, new HashMap<String, String>());
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
            s3Provider.getSpaceAccess(spaceId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.setSpaceAccess(spaceId, AccessType.CLOSED);
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
            s3Provider.getContentMetadata(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.setContentMetadata(spaceId,
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
            s3Provider.getContentMetadata(spaceId, contentId);
            fail(failMsg);
        } catch (NotFoundException expected) {
            assertNotNull(expected);
        }

        try {
            s3Provider.setContentMetadata(spaceId,
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
    public void testSetContentMetadata() {
        // StorageProvider metadata which is expected to be available
        String name00 = StorageProvider.METADATA_CONTENT_CHECKSUM;
        String value00 = "c56f855f5dec9276733ff3e2c66ec7df";
        String name01 = StorageProvider.METADATA_CONTENT_MD5;
        String value01 = "c56f855f5dec9276733ff3e2c66ec7df";
        String name02 = StorageProvider.METADATA_CONTENT_MIMETYPE;
        String value02 = "text/html";
        String name03 = StorageProvider.METADATA_CONTENT_SIZE;
        String value03 = "59142";
        String name04 = StorageProvider.METADATA_CONTENT_MODIFIED;
        String value04 = "Wed, 25 Nov 2009 11:09:39 EST";

        // Custom metadata values
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

        Map<String, String> contentMetadata = new HashMap<String, String>();
        contentMetadata.put(name00, value00);
        contentMetadata.put(name01, value01);
        contentMetadata.put(name02, value02);
        contentMetadata.put(name03, value03);
        contentMetadata.put(name04, value04);
        contentMetadata.put(name05, value05);
        contentMetadata.put(name06, value06);
        contentMetadata.put(name07, value07);
        contentMetadata.put(name08, value08);
        contentMetadata.put(name09, value09);
        contentMetadata.put(name10, value10);
        contentMetadata.put(name11, value11);
        contentMetadata.put(name12, value12);
        contentMetadata.put(name13, value13);

        // Set up the space and content
        String spaceId = getNewSpaceId();
        String contentId = getNewContentId();

        s3Provider.createSpace(spaceId);
        addContent(spaceId, contentId, CONTENT_MIME_VALUE, false);

        // This is the method under test.
        s3Provider.setContentMetadata(spaceId, contentId, contentMetadata);

        Map<String, String> metadata = s3Provider.getContentMetadata(spaceId,
                                                                     contentId);
        Assert.assertNotNull(metadata);

        Assert.assertTrue(metadata.containsKey(name00));
        Assert.assertTrue(metadata.containsKey(name01));
        Assert.assertTrue(metadata.containsKey(name02));
        Assert.assertTrue(metadata.containsKey(name03));
        Assert.assertTrue(metadata.containsKey(name04));
        Assert.assertTrue(metadata.containsKey(name05));
        Assert.assertTrue(metadata.containsKey(name06));
        Assert.assertTrue(metadata.containsKey(name07));
        Assert.assertTrue(metadata.containsKey(name08.toLowerCase()));
        Assert.assertTrue(metadata.containsKey(name09));
        Assert.assertTrue(metadata.containsKey(name10));
        Assert.assertTrue(metadata.containsKey(name11));
        Assert.assertTrue(metadata.containsKey(name12));
        Assert.assertFalse(metadata.containsKey(name13));

        Assert.assertNotNull(metadata.get(name00));
        Assert.assertNotNull(metadata.get(name01));
        Assert.assertNotNull(metadata.get(name02));
        Assert.assertNotNull(metadata.get(name03));
        Assert.assertNotNull(metadata.get(name04));
        Assert.assertNotNull(metadata.get(name05));
        Assert.assertNotNull(metadata.get(name06));
        Assert.assertNotNull(metadata.get(name07));
        Assert.assertNotNull(metadata.get(name08.toLowerCase()));
        Assert.assertNotNull(metadata.get(name09));
        Assert.assertNotNull(metadata.get(name10));
        Assert.assertNotNull(metadata.get(name11));
        Assert.assertNotNull(metadata.get(name12));
    }

}
