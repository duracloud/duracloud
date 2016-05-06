/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.duracloud.common.rest.HttpHeaders;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Bill Branan
 *         Date: Sep 10, 2010
 */
public class ContentRestTest {

    private ContentRest contentRest;

    private ContentResource contentResource;
    private RestUtil restUtil;
    private HttpHeaders httpHeaders;
    private UriInfo uriInfo;
    private HttpServletRequest request;

    private static final String srcSpaceId = "srcSpaceId";
    private static final String srcContentId = "src/content/id";
    private static final String destSpaceId = "destSpaceId";
    private static final String destContentId = "destContentId";
    private static final String storeId = "0";
    private static final String copySource = srcSpaceId + "/" + srcContentId;


    @Before
    public void setUp() {
        contentResource = EasyMock.createMock("ContentResource",
                                              ContentResource.class);
        restUtil = EasyMock.createMock("RestUtil", RestUtil.class);
        httpHeaders = EasyMock.createMock("HttpHeaders", HttpHeaders.class);
        uriInfo = EasyMock.createMock("UriInfo", UriInfo.class);
        request = EasyMock.createMock("HttpServletRequest",
                                      HttpServletRequest.class);

    }

    @After
    public void tearDown() throws IOException {
        EasyMock.verify(contentResource,
                        restUtil,
                        httpHeaders,
                        uriInfo,
                        request);
    }

    private void replayMocks() {
        EasyMock.replay(contentResource,
                        restUtil,
                        httpHeaders,
                        uriInfo,
                        request);
    }

    @Test
    public void testAddContentPropertiesToResponse() {
        Response response;

        replayMocks();
        contentRest = new ContentRest(null, null);

        String contentType = "text/contenttype";
        String contentMimetype = "text/contentmimetype";
        String contentLength = "12345";
        String contentSize = "67890";
        String lastModified = "lastmodified";
        String contentModified = "contentmodified";
        String etag = "etag";
        String contentmdfive = "contentmdfive";
        String contentmd5 = "conentmd5";
        String contentChecksum = "contentchecksum";

        Map<String, String> props = new HashMap<>();

        // Verify Content-Type is used if only option
        props.put(HttpHeaders.CONTENT_TYPE, contentType);
        response = contentRest.addContentPropertiesToResponse(Response.ok(), props);
        assertEquals(contentType, getHeader(response, HttpHeaders.CONTENT_TYPE));

        // Verify content-mimetype chosen over Content-Type
        props.put(StorageProvider.PROPERTIES_CONTENT_MIMETYPE, contentMimetype);
        response = contentRest.addContentPropertiesToResponse(Response.ok(), props);
        assertEquals(contentMimetype, getHeader(response, HttpHeaders.CONTENT_TYPE));

        // Verify Content-Length used if only option
        props.put(HttpHeaders.CONTENT_LENGTH, contentLength);
        response = contentRest.addContentPropertiesToResponse(Response.ok(), props);
        assertEquals(contentLength, getHeader(response, HttpHeaders.CONTENT_LENGTH));

        // Verify content-size chosen over Content-Length
        props.put(StorageProvider.PROPERTIES_CONTENT_SIZE, contentSize);
        response = contentRest.addContentPropertiesToResponse(Response.ok(), props);
        assertEquals(contentSize, getHeader(response, HttpHeaders.CONTENT_LENGTH));

        // Verify Last-Modified used if only option
        props.put(HttpHeaders.LAST_MODIFIED, lastModified);
        response = contentRest.addContentPropertiesToResponse(Response.ok(), props);
        assertEquals(lastModified, getHeader(response, HttpHeaders.LAST_MODIFIED));

        // Verify content-modified chosen over Last-Modified
        props.put(StorageProvider.PROPERTIES_CONTENT_MODIFIED, contentModified);
        response = contentRest.addContentPropertiesToResponse(Response.ok(), props);
        assertEquals(contentModified, getHeader(response, HttpHeaders.LAST_MODIFIED));

        // Verify ETag used if only option
        props.put(HttpHeaders.ETAG, etag);
        response = contentRest.addContentPropertiesToResponse(Response.ok(), props);
        assertEquals(etag, getHeader(response, HttpHeaders.ETAG));
        assertEquals(etag, getHeader(response, HttpHeaders.CONTENT_MD5));

        // Verify Content-MD5 chosen over ETag
        props.put(HttpHeaders.CONTENT_MD5, contentmd5);
        response = contentRest.addContentPropertiesToResponse(Response.ok(), props);
        assertEquals(contentmd5, getHeader(response, HttpHeaders.ETAG));
        assertEquals(contentmd5, getHeader(response, HttpHeaders.CONTENT_MD5));

        // Verify content-md5 chosen over Content-MD5 and ETag
        props.put(StorageProvider.PROPERTIES_CONTENT_MD5, contentmdfive);
        response = contentRest.addContentPropertiesToResponse(Response.ok(), props);
        assertEquals(contentmdfive, getHeader(response, HttpHeaders.ETAG));
        assertEquals(contentmdfive, getHeader(response, HttpHeaders.CONTENT_MD5));

        // Verify content-checksum chosen over content-md5, Content-MD5, and ETag
        props.put(StorageProvider.PROPERTIES_CONTENT_CHECKSUM, contentChecksum);
        response = contentRest.addContentPropertiesToResponse(Response.ok(), props);
        assertEquals(contentChecksum, getHeader(response, HttpHeaders.ETAG));
        assertEquals(contentChecksum, getHeader(response, HttpHeaders.CONTENT_MD5));

        // Verify date and connection are left out
        props.put(HttpHeaders.DATE, "date");
        props.put(HttpHeaders.CONNECTION, "connection");
        response = contentRest.addContentPropertiesToResponse(Response.ok(), props);
        assertHeaderMissing(response, HttpHeaders.DATE);
        assertHeaderMissing(response, HttpHeaders.CONNECTION);
        
        // Verify standard http headers are passed through
        String age = "age";
        String cacheControl = "cachecontrol";
        String contentEncoding = "contentencoding";
        String contentLanguage = "contentlanguage";
        String contentLocation = "contentlocation";
        String contentRange = "contentrange";
        String expires = "expires";
        String location = "location";
        String pragma = "pragma";
        String retryAfter = "retryafter";
        String server = "server";
        String transferEncoding = "transferEncoding";
        String upgrade = "upgrade";
        String warning = "warning";

        props.put(HttpHeaders.AGE, age);
        props.put(HttpHeaders.CACHE_CONTROL, cacheControl);
        props.put(HttpHeaders.CONTENT_ENCODING, contentEncoding);
        props.put(HttpHeaders.CONTENT_LANGUAGE, contentLanguage);
        props.put(HttpHeaders.CONTENT_LOCATION, contentLocation);
        props.put(HttpHeaders.CONTENT_RANGE, contentRange);
        props.put(HttpHeaders.EXPIRES, expires);
        props.put(HttpHeaders.LOCATION, location);
        props.put(HttpHeaders.PRAGMA, pragma);
        props.put(HttpHeaders.RETRY_AFTER, retryAfter);
        props.put(HttpHeaders.SERVER, server);
        props.put(HttpHeaders.TRANSFER_ENCODING, transferEncoding);
        props.put(HttpHeaders.UPGRADE, upgrade);
        props.put(HttpHeaders.WARNING, warning);
        
        response = contentRest.addContentPropertiesToResponse(Response.ok(), props);
        assertEquals(age, getHeader(response, HttpHeaders.AGE));
        assertEquals(cacheControl, getHeader(response, HttpHeaders.CACHE_CONTROL));
        assertEquals(contentEncoding, getHeader(response, HttpHeaders.CONTENT_ENCODING));
        assertEquals(contentLanguage, getHeader(response, HttpHeaders.CONTENT_LANGUAGE));
        assertEquals(contentLocation, getHeader(response, HttpHeaders.CONTENT_LOCATION));
        assertEquals(contentRange, getHeader(response, HttpHeaders.CONTENT_RANGE));
        assertEquals(expires, getHeader(response, HttpHeaders.EXPIRES));
        assertEquals(location, getHeader(response, HttpHeaders.LOCATION));
        assertEquals(pragma, getHeader(response, HttpHeaders.PRAGMA));
        assertEquals(retryAfter, getHeader(response, HttpHeaders.RETRY_AFTER));
        assertEquals(server, getHeader(response, HttpHeaders.SERVER));
        assertEquals(transferEncoding, getHeader(response, HttpHeaders.TRANSFER_ENCODING));
        assertEquals(upgrade, getHeader(response, HttpHeaders.UPGRADE));
        assertEquals(warning, getHeader(response, HttpHeaders.WARNING));

        // Verify custom properties are passed back with the expected prefix
        String customNameOne = "custom-name-one";
        String customValueOne = "Custom Value One!!";
        String customNameTwo = "custom-name-two";
        String customValueTwo = "CuSTom Value 2";
        props.put(customNameOne, customValueOne);
        props.put(customNameTwo, customValueTwo);
        response = contentRest.addContentPropertiesToResponse(Response.ok(), props);
        assertEquals(customValueOne,
                     getHeader(response, ContentRest.HEADER_PREFIX + customNameOne));
        assertEquals(customValueTwo,
                     getHeader(response, ContentRest.HEADER_PREFIX + customNameTwo));

        // Verify no internal content-* headers are passed through
        assertHeaderMissing(response, StorageProvider.PROPERTIES_CONTENT_MIMETYPE);
        assertHeaderMissing(response, StorageProvider.PROPERTIES_CONTENT_SIZE);
        assertHeaderMissing(response, StorageProvider.PROPERTIES_CONTENT_MODIFIED);
        assertHeaderMissing(response, StorageProvider.PROPERTIES_CONTENT_CHECKSUM);

        // Verify only one value for each header
        for(String headerName : response.getMetadata().keySet()) {
            assertEquals(1, response.getMetadata().get(headerName).size());
        }
    }

    private String getHeader(Response response, String headerName) {
        return (String)response.getMetadata().get(headerName).get(0);
    }

    private void assertHeaderMissing(Response response, String headerName) {
        assertNull(response.getMetadata().get(headerName));
    }

    @Test
    public void testValidMimetype() {
        replayMocks();
        contentRest = new ContentRest(null, null);

        assertTrue(contentRest.validMimetype("text/xml"));
        assertTrue(contentRest.validMimetype("application/xml"));
        assertTrue(contentRest.validMimetype("blah/blah"));

        assertFalse(contentRest.validMimetype("text*xml"));
        assertFalse(contentRest.validMimetype("***"));
    }

    @Test
    public void testCopyContent() throws Exception {
        doTestCopyContent(true, 201);
    }

    @Test
    public void testCopyContentError() throws Exception {
        doTestCopyContent(false, 400);
    }

    private void doTestCopyContent(boolean valid, int status) throws Exception {
        if (valid) {
            createCopyContentMocks();
        } else {
            createCopyContentMocksError();
        }

        replayMocks();
        contentRest = new ContentRest(contentResource, restUtil);
        contentRest.headers = httpHeaders;
        contentRest.uriInfo = uriInfo;
        contentRest.request = request;

        Response response = contentRest.putContent(destSpaceId,
                                                   destContentId,
                                                   storeId,
                                                   copySource,
                                                   storeId);
        Assert.assertNotNull(response);
        assertEquals(status, response.getStatus());
    }

    private void createCopyContentMocks() throws Exception {
        // request content
        EasyMock.expect(restUtil.getRequestContent(request, httpHeaders))
            .andReturn(null);

        // uriInfo
        EasyMock.expect(uriInfo.getRequestUri()).andReturn(new URI(
            "/" + destSpaceId + "/" + destContentId));

        // contentResource
        EasyMock.expect(contentResource.copyContent(storeId,
                                                    srcSpaceId,
                                                    srcContentId,
                                                    storeId,
                                                    destSpaceId,
                                                    destContentId)).andReturn("md5");
    }

    private void createCopyContentMocksError() throws Exception {
        RestUtil.RequestContent content = EasyMock.createMock("RequestContent",
                                                              RestUtil.RequestContent.class);
        
        EasyMock.expect(content.getSize()).andReturn(5l).times(2);
        EasyMock.replay(content);

        EasyMock.expect(restUtil.getRequestContent(request, httpHeaders))
            .andReturn(content);
    }

}
