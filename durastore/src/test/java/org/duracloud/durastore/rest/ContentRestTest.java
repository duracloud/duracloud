/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.rest.RestUtil;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.io.IOException;
import java.net.URI;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

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
        Assert.assertEquals(status, response.getStatus());
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
