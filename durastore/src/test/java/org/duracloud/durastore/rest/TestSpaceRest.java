/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Runtime test of space REST API. The durastore
 * web application must be deployed and available at the
 * baseUrl location in order for these tests to pass.
 *
 * @author Bill Branan
 */
public class TestSpaceRest extends BaseRestTester {

    private static List<String> spaces;

    @Before
    public void setUp() throws Exception {
        // Add space
        setNewSpaceId();
        HttpResponse response = RestTestHelper.addSpace(spaceId);
        String responseText = checkResponse(response, HttpStatus.SC_CREATED);

        spaces = new ArrayList<String>();
        spaces.add(spaceId);
    }

    @After
    public void tearDown() throws Exception {
        for(String spaceId : spaces) {
            RestTestHelper.deleteSpace(spaceId);
        }
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
        HttpResponse response = RestTestHelper.addSpace(id);
        checkResponse(response, HttpStatus.SC_BAD_REQUEST);
    }

    private void checkValidSpaceId(String id) throws Exception {
        spaces.add(id);
        HttpResponse response = RestTestHelper.addSpace(id);
        checkResponse(response, HttpStatus.SC_CREATED);
    }

    @Test
    public void testGetSpaces() throws Exception {
        String url = baseUrl + "/spaces";
        HttpResponse response = restHelper.get(url);
        String responseText = checkResponse(response, HttpStatus.SC_OK);
        assertTrue(responseText.contains("<spaces>"));
    }

    @Test
    public void testGetSpaceMetadata() throws Exception {
        String url = baseUrl + "/" + spaceId;
        HttpResponse response = restHelper.head(url);
        checkResponse(response, HttpStatus.SC_OK);

        testMetadata(response,
                     BaseRest.SPACE_ACCESS_HEADER,
                     RestTestHelper.SPACE_ACCESS);
        testMetadata(response,
                     RestTestHelper.METADATA_NAME,
                     RestTestHelper.METADATA_VALUE);
    }

    @Test
    public void testGetSpaceContents() throws Exception {
        addContent();

        String spaceUrl = baseUrl + "/" + spaceId;

        // Get complete list
        String url = spaceUrl;
        HttpResponse response = restHelper.get(url);
        String responseText = checkResponse(response, HttpStatus.SC_OK);
        List<String> contentIds = parseContentList(responseText);
        assertEquals(3, contentIds.size());

        // MaxResults + Marker test
        url = spaceUrl + "?maxResults=2";
        response = restHelper.get(url);
        responseText = checkResponse(response, HttpStatus.SC_OK);
        contentIds = parseContentList(responseText);
        assertEquals(2, contentIds.size());

        String lastContentId = contentIds.get(contentIds.size() - 1);
        url = spaceUrl + "?maxResults=2&marker=" + lastContentId;
        response = restHelper.get(url);
        responseText = checkResponse(response, HttpStatus.SC_OK);
        contentIds = parseContentList(responseText);
        assertEquals(1, contentIds.size());

        // Prefix test
        url = spaceUrl + "?prefix=test";
        response = restHelper.get(url);
        responseText = checkResponse(response, HttpStatus.SC_OK);
        contentIds = parseContentList(responseText);
        assertEquals(2, contentIds.size());
        for(String contentId : contentIds) {
            assertTrue(contentId.startsWith("test"));
        }        
    }

    private void addContent() throws Exception {
        String content = "<content />";
        String[] contentIds = {"test1", "test2", "check3"};

        // Add content to space
        for(String contentId : contentIds) {
            String url = baseUrl + "/" + spaceId + "/" + contentId;
            HttpResponse response = restHelper.put(url, content, null);
            checkResponse(response, HttpStatus.SC_CREATED);
        }
    }

    private List<String> parseContentList(String responseText)
        throws Exception {
        InputStream is =
            new ByteArrayInputStream(responseText.getBytes());
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(is);
        Element spaceElem = doc.getRootElement();

        List<String> contentItems = new ArrayList<String>();
        Iterator<?> spaceContents = spaceElem.getChildren().iterator();
        while (spaceContents.hasNext()) {
            Element contentElem = (Element) spaceContents.next();
            contentItems.add(contentElem.getTextTrim());
        }
        return contentItems;
    }

    @Test
    public void testUpdateSpaceMetadata() throws Exception {
        String url = baseUrl + "/" + spaceId;

        // Add metadata
        Map<String, String> headers = new HashMap<String, String>();
        String newSpaceAccess = "CLOSED";
        headers.put(BaseRest.SPACE_ACCESS_HEADER, newSpaceAccess);
        String newSpaceMetadata = "Updated Space Metadata";
        headers.put(RestTestHelper.METADATA_NAME, newSpaceMetadata);
        HttpResponse response = restHelper.post(url, null, headers);

        String responseText = checkResponse(response, HttpStatus.SC_OK);
        assertNotNull(responseText);
        assertTrue(responseText.contains(spaceId));
        assertTrue(responseText.contains("updated"));

        // Make sure the changes were saved
        response = restHelper.head(url);
        checkResponse(response, HttpStatus.SC_OK);

        testMetadata(response, BaseRest.SPACE_ACCESS_HEADER, newSpaceAccess);
        testMetadata(response, RestTestHelper.METADATA_NAME, newSpaceMetadata);

        // Remove metadata
        headers.remove(RestTestHelper.METADATA_NAME);
        response = restHelper.post(url, null, headers);

        responseText = checkResponse(response, HttpStatus.SC_OK);
        assertNotNull(responseText);
        assertTrue(responseText.contains(spaceId));
        assertTrue(responseText.contains("updated"));

        response = restHelper.head(url);
        checkResponse(response, HttpStatus.SC_OK);

        testNoMetadata(response, RestTestHelper.METADATA_NAME);
    }

    private void testMetadata(HttpResponse response, String name, String value)
            throws Exception {
        String metadata = response.getResponseHeader(name).getValue();
        assertNotNull(metadata);
        assertEquals(metadata, value);
    }

    private void testNoMetadata(HttpResponse response,
                                String name) throws Exception {
        assertNull(response.getResponseHeader(name));
    }

    @Test
    public void testNotFound() throws Exception {
        String invalidSpaceId = "non-existant-space";
        String url = baseUrl + "/" + invalidSpaceId;

        // Get Space
        HttpResponse response = restHelper.get(url);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);

        // Get Space Metadata
        response = restHelper.head(url);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);

        // Set Space Metadata
        response = restHelper.post(url, null, null);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);

        // Delete Space
        response = restHelper.delete(url);
        checkResponse(response, HttpStatus.SC_NOT_FOUND);
    }

 }