/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.rest.HttpHeaders;
import org.duracloud.common.web.EncodeUtil;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Runtime test of content REST API. The durastore web application must be
 * deployed and available at the baseUrl location in order for these tests to
 * pass.
 *
 * @author Bill Branan
 */
public class TestContentRest extends BaseRestTester {

    private static final String CONTENT = "<content />";

    private static String[] contentIds = {"content1",
                                          "dir0/dir1/content2",
                                          "dir0/dir1/content3?storeID=0"};

    @Before
    public void setUp() throws Exception {
        // Add space
        setNewSpaceId();
        HttpResponse response = RestTestHelper.addSpace(spaceId);
        checkResponse(response, HttpStatus.SC_CREATED);

        for (String contentId : contentIds) {
            setUpContent(contentId);
        }
    }

    private void setUpContent(String contentId) throws Exception {
        HttpResponse response;

        // Add content to space
        String url = baseUrl + "/" + spaceId + "/" + contentId;
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(RestTestHelper.METADATA_NAME,
                    RestTestHelper.METADATA_VALUE);
        response = restHelper.put(url, CONTENT, headers);
        checkResponse(response, HttpStatus.SC_CREATED);
    }

    @After
    public void tearDown() throws Exception {
        // Delete space
        HttpResponse response = RestTestHelper.deleteSpace(spaceId);
        String responseText = checkResponse(response, HttpStatus.SC_OK);
        assertNotNull(responseText);
    }

    private String removeParams(String contentId) {
        int paramStart = contentId.indexOf('?');
        int endIndex = paramStart > 0 ? paramStart : contentId.length();
        return contentId.substring(0, endIndex);
    }

    @Test
    public void testAddContent() throws Exception {
        // Test invalid content IDs

        // Question mark
        String id = "test?content";
        id = EncodeUtil.urlEncode(id);
        HttpResponse response = addContentItem(id);
        checkResponse(response, HttpStatus.SC_BAD_REQUEST);

        // Backslash
        id = "test\\content";
        id = EncodeUtil.urlEncode(id);
        response = addContentItem(id);
        checkResponse(response, HttpStatus.SC_BAD_REQUEST);

        // Too long
        id = "test-content";
        while(id.getBytes().length <= 1024) {
            id += "test-content";
        }
        response = addContentItem(id);
        checkResponse(response, HttpStatus.SC_BAD_REQUEST);

        // Test valid content IDs

        // Test Special characters
        char[] specialChars = {'~','`','!','@','$','^','&','*','(',')','_','-',
                               '+','=','\'',':','.',',','<','>','"','[',']',
                               '{','}','#','%',';','|',' ','/'};
        for(char character : specialChars) {
            testCharacterInContentId(character);
        }
    }

    private HttpResponse addContentItem(String contentId) throws Exception {
        String url = baseUrl + "/" + spaceId + "/" + contentId;
        Map<String, String> headers = new HashMap<String, String>();
        return restHelper.put(url, CONTENT, headers);
    }

    private void testCharacterInContentId(char character) throws Exception {
        String contentId = "test-" + String.valueOf(character) + "-content";
        contentId = EncodeUtil.urlEncode(contentId);
        HttpResponse response = addContentItem(contentId);
        assertEquals("Testing character: " + character,
                     HttpStatus.SC_CREATED,
                     response.getStatusCode());
        response = getContentItem(contentId);
        checkResponse(response, HttpStatus.SC_OK);
    }

    private HttpResponse getContentItem(String contentId) throws Exception {
        String url = baseUrl + "/" + spaceId + "/" + contentId;
        return restHelper.get(url);
    }

    @Test
    public void testGetContent() throws Exception {
        for (String contentId : contentIds) {
            doTestGetContent(contentId);
        }
    }

    private void doTestGetContent(String contentId) throws Exception {
        HttpResponse response = getContentItem(contentId);
        String responseText = checkResponse(response, HttpStatus.SC_OK);
        assertNotNull(responseText);
        assertEquals(CONTENT, responseText);

        String contentType =
            response.getResponseHeader(HttpHeaders.CONTENT_TYPE).getValue();
        assertNotNull(contentType);
        assertTrue(contentType.contains("text/xml"));
    }

    @Test
    public void testGetContentMetadata() throws Exception {
        for (String contentId : contentIds) {
            doTestGetContentMetadata(contentId);
        }
    }

    private void doTestGetContentMetadata(String contentId) throws Exception {
        String url = baseUrl + "/" + spaceId + "/" + contentId;
        HttpResponse response = restHelper.head(url);
        checkResponse(response, HttpStatus.SC_OK);

        verifyMetadata(response, HttpHeaders.CONTENT_LENGTH, "11");

        String contentType =
            response.getResponseHeader(HttpHeaders.CONTENT_TYPE).getValue();
        assertNotNull(contentType);
        assertTrue(contentType.contains("text/xml"));

        String contentChecksum =
            response.getResponseHeader(HttpHeaders.CONTENT_MD5).getValue();
        assertNotNull(contentChecksum);

        String contentETag =
            response.getResponseHeader(HttpHeaders.ETAG).getValue();
        assertNotNull(contentETag);
        assertEquals(contentChecksum, contentETag);

        String contentModified =
            response.getResponseHeader(HttpHeaders.LAST_MODIFIED).getValue();
        assertNotNull(contentModified);

        verifyMetadata(response,
                       RestTestHelper.METADATA_NAME,
                       RestTestHelper.METADATA_VALUE);
    }

    @Test
    public void testUpdateContentMetadata() throws Exception {
        for (String contentId : contentIds) {
            doTestUpdateContentMetadata(contentId);
        }
    }

    private void doTestUpdateContentMetadata(String contentId)
        throws Exception {
        String url = baseUrl + "/" + spaceId + "/" + contentId;

        // Add metadata
        Map<String, String> headers = new HashMap<String, String>();
        String newContentMime = "text/plain";
        headers.put(HttpHeaders.CONTENT_TYPE, newContentMime);
        String newMetaName = BaseRest.HEADER_PREFIX + "new-metadata";
        String newMetaValue = "New Metadata Value";
        headers.put(newMetaName, newMetaValue);

        postMetadataUpdate(url, contentId, headers);

        // Make sure the changes were saved
        HttpResponse response = restHelper.head(url);
        checkResponse(response, HttpStatus.SC_OK);

        verifyMetadata(response, HttpHeaders.CONTENT_TYPE, newContentMime);
        verifyMetadata(response, newMetaName, newMetaValue);

        // Remove metadata
        headers = new HashMap<String, String>();
        postMetadataUpdate(url, contentId, headers);

        response = restHelper.head(url);
        checkResponse(response, HttpStatus.SC_OK);

        // New metadata items should be gone, mimetype should be unchanged
        verifyNoMetadata(response, newMetaName);
        verifyMetadata(response, HttpHeaders.CONTENT_TYPE, newContentMime);

        // Update mimetype
        String testMime = "application/test";
        headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, testMime);
        postMetadataUpdate(url, contentId, headers);

        response = restHelper.head(url);
        checkResponse(response, HttpStatus.SC_OK);

        // Metadata should be updated
        verifyMetadata(response, HttpHeaders.CONTENT_TYPE, testMime);
    }

    private HttpResponse postMetadataUpdate(String url,
                                            String contentId,
                                            Map<String, String> headers)
        throws Exception {
        HttpResponse response = restHelper.post(url, null, headers);
        String responseText = checkResponse(response, HttpStatus.SC_OK);
        assertNotNull(responseText);
        assertTrue(responseText.contains(removeParams(contentId)));
        assertTrue(responseText.contains("updated"));

        return response;
    }

    private void verifyMetadata(HttpResponse response,
                                String name,
                                String value) throws Exception {
        String metadata = response.getResponseHeader(name).getValue();
        assertNotNull(metadata);
        assertEquals(metadata, value);
    }

    private void verifyNoMetadata(HttpResponse response,
                                  String name) throws Exception {
        assertNull(response.getResponseHeader(name));
    }

    @Test
    public void testNotFound() throws Exception {
        String invalidSpaceId = "non-existant-space";
        String invalidContentId = "non-existant-content";
        String url = baseUrl + "/" + invalidSpaceId + "/" + invalidContentId;

        // Add Content
        HttpResponse response = restHelper.put(url, "test-content", null);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);

        url = baseUrl + "/" + spaceId + "/" + invalidContentId;

        // Get Content
        response = restHelper.get(url);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);

        // Get Content Metadata
        response = restHelper.head(url);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);

        // Set Content Metadata
        response = restHelper.post(url, null, null);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);

        // Delete Content
        response = restHelper.delete(url);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);
    } 

}