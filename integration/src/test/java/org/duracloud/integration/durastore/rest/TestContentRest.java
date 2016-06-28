/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.durastore.rest;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.duracloud.client.HttpHeaders;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.web.EncodeUtil;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
                                          "dir0/dir1/content2"};

    @Before
    public void setUp() throws Exception {
        // Add space
        setNewSpaceId();
        HttpResponse response = RestTestHelper.addSpace(BaseRestTester.spaceId);
        checkResponse(response, HttpStatus.SC_CREATED);

        for (String contentId : contentIds) {
            setUpContent(contentId);
        }
    }

    private void setUpContent(String contentId) throws Exception {
        HttpResponse response;

        // Add content to space
        String url = BaseRestTester.baseUrl + "/" + BaseRestTester.spaceId + "/" + contentId;
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(RestTestHelper.PROPERTIES_NAME,
                    RestTestHelper.PROPERTIES_VALUE);
        response = BaseRestTester.restHelper.put(url, CONTENT, headers);
        checkResponse(response, HttpStatus.SC_CREATED);
    }

    @After
    public void tearDown() throws Exception {
        // Delete space
        HttpResponse response = RestTestHelper.deleteSpace(BaseRestTester.spaceId);
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
        String url = BaseRestTester.baseUrl + "/" + BaseRestTester.spaceId + "/" + contentId;
        Map<String, String> headers = new HashMap<String, String>();
        return BaseRestTester.restHelper.put(url,
                                             new ByteArrayInputStream(CONTENT.getBytes()),
                                             "text/plain",
                                             CONTENT.length(),
                                             headers);
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
        String url = BaseRestTester.baseUrl + "/" + BaseRestTester.spaceId + "/" + contentId;
        return BaseRestTester.restHelper.get(url);
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
    public void testGetContentProperties() throws Exception {
        for (String contentId : contentIds) {
            doTestGetContentProperties(contentId);
        }
    }

    private void doTestGetContentProperties(String contentId) throws Exception {
        String url = BaseRestTester.baseUrl + "/" + BaseRestTester.spaceId + "/" + contentId;
        HttpResponse response = BaseRestTester.restHelper.head(url);
        checkResponse(response, HttpStatus.SC_OK);

        verifyProperties(response, HttpHeaders.CONTENT_LENGTH, "11");

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

        verifyProperties(response,
                       RestTestHelper.PROPERTIES_NAME,
                       RestTestHelper.PROPERTIES_VALUE);
    }

    @Test
    public void testUpdateContentProperties() throws Exception {
        for (String contentId : contentIds) {
            doTestUpdateContentProperties(contentId);
        }
    }

    private void doTestUpdateContentProperties(String contentId)
        throws Exception {
        String url = BaseRestTester.baseUrl + "/" + BaseRestTester.spaceId + "/" + contentId;

        // Add properties
        Map<String, String> headers = new HashMap<String, String>();
        String newContentMime = "text/plain";
        headers.put(HttpHeaders.CONTENT_TYPE, newContentMime);
        String newMetaName = Constants.HEADER_PREFIX + "new-properties";
        String newMetaValue = "New Properties Value";
        headers.put(newMetaName, newMetaValue);

        postPropertiesUpdate(url, contentId, headers);

        // Make sure the changes were saved
        HttpResponse response = BaseRestTester.restHelper.head(url);
        checkResponse(response, HttpStatus.SC_OK);

        verifyProperties(response, HttpHeaders.CONTENT_TYPE, newContentMime);
        verifyProperties(response, newMetaName, newMetaValue);

        // Remove properties
        headers = new HashMap<String, String>();
        postPropertiesUpdate(url, contentId, headers);

        response = BaseRestTester.restHelper.head(url);
        checkResponse(response, HttpStatus.SC_OK);

        // New properties items should be gone, mimetype should be unchanged
        verifyNoProperties(response, newMetaName);
        verifyProperties(response, HttpHeaders.CONTENT_TYPE, newContentMime);

        // Update mimetype
        String testMime = "application/test";
        headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, testMime);
        postPropertiesUpdate(url, contentId, headers);

        response = BaseRestTester.restHelper.head(url);
        checkResponse(response, HttpStatus.SC_OK);

        // Properties should be updated
        verifyProperties(response, HttpHeaders.CONTENT_TYPE, testMime);

        // Attempt to update to invalid mime
        String invalidMime = "application*test";
        headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, invalidMime);
        postInvalidPropertiesUpdate(url, contentId, headers, HttpStatus.SC_BAD_REQUEST);

        response = BaseRestTester.restHelper.head(url);
        checkResponse(response, HttpStatus.SC_OK);

        // Properties should not have been updated
        verifyProperties(response, HttpHeaders.CONTENT_TYPE, testMime);
    }

    private HttpResponse postPropertiesUpdate(String url,
                                            String contentId,
                                            Map<String, String> headers)
        throws Exception {
        HttpResponse response = BaseRestTester.restHelper.post(url, null, headers);
        String responseText = checkResponse(response, HttpStatus.SC_OK);
        assertNotNull(responseText);
        assertTrue(responseText.contains(removeParams(contentId)));
        assertTrue(responseText.contains("updated"));

        return response;
    }

    private HttpResponse postInvalidPropertiesUpdate(String url,
                                                     String contentId,
                                                     Map<String, String> headers)
          throws Exception {

        return postInvalidPropertiesUpdate(url, contentId, headers, HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
    private HttpResponse postInvalidPropertiesUpdate(String url,
                                                   String contentId,
                                                   Map<String, String> headers,int status)
        throws Exception {
        HttpResponse response =
            BaseRestTester.restHelper.post(url, null, headers);
        String responseText =
            checkResponse(response,status);
        assertNotNull(responseText);
        return response;
    }

    private void verifyProperties(HttpResponse response,
                                String name,
                                String value) throws Exception {
        String properties = response.getResponseHeader(name).getValue();
        assertNotNull(properties);
        assertEquals(properties, value);
    }

    private void verifyNoProperties(HttpResponse response,
                                  String name) throws Exception {
        assertNull(response.getResponseHeader(name));
    }

    @Test
    public void testNotFound() throws Exception {
        String invalidSpaceId = "non-existant-space";
        String invalidContentId = "non-existant-content";
        String url = BaseRestTester.baseUrl + "/" + invalidSpaceId + "/" + invalidContentId;

        // Add Content
        HttpResponse response = BaseRestTester.restHelper.put(url, "test-content", null);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);

        url = BaseRestTester.baseUrl + "/" + BaseRestTester.spaceId + "/" + invalidContentId;

        // Get Content
        response = BaseRestTester.restHelper.get(url);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);

        // Get Content Properties
        response = BaseRestTester.restHelper.head(url);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);

        // Set Content Properties
        response = BaseRestTester.restHelper.post(url, null, null);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);

        // Delete Content
        response = BaseRestTester.restHelper.delete(url);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);
    } 

}
