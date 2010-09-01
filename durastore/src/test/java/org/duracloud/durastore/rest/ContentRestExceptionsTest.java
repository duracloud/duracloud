/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.rest.RestUtil;
import org.duracloud.durastore.error.ResourceException;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 * This class tests top-level error handling of ContentRest.
 *
 * @author Andrew Woods
 *         Date: Aug 31, 2010
 */
public class ContentRestExceptionsTest {

    private ContentRest contentRest;
    private ContentResource contentResource;
    private RestUtil restUtil;

    private RestExceptionsTestSupport support = new RestExceptionsTestSupport();

    @Before
    public void setUp() throws Exception {
        contentResource = createContentResource();
        restUtil = support.createRestUtil();
        contentRest = new ContentRest(contentResource, restUtil);
    }

    private ContentResource createContentResource() throws Exception {
        ContentResource resource = EasyMock.createMock("ContentResource",
                                                       ContentResource.class);

        EasyMock.expect(resource.getContentMetadata(null, null, null)).andThrow(
            support.createRuntimeException()).anyTimes();
        resource.deleteContent(null, null, null);
        EasyMock.expectLastCall()
            .andThrow(support.createRuntimeException())
            .anyTimes();

        EasyMock.replay(resource);
        return resource;

    }

    @Test
    public void testGetContent() throws Exception {
        Response response = contentRest.getContent(null, null, null, false);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testGetContentMetadata() throws Exception {
        Response response = contentRest.getContentMetadata(null, null, null);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testUpdateContentMetadata() throws Exception {
        Response response = contentRest.updateContentMetadata(null, null, null);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testAddContent() throws Exception {
        Response response = contentRest.addContent(null, null, null);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testDeleteContent() throws Exception {
        Response response = contentRest.deleteContent(null, null, null);
        support.verifyErrorResponse(response);
    }
}
