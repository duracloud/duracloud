/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.duracloud.common.rest.RestUtil;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;

import javax.ws.rs.core.Response;

/**
 * @author: Bill Branan
 * Date: 9/19/11
 */
public class RestExceptionsTestSupport {

    protected void verifyErrorResponse(Response response) {
        int expectedStatus = Response.Status
            .INTERNAL_SERVER_ERROR
            .getStatusCode();
        verifyErrorResponse(response, expectedStatus);
    }

    protected void verifyErrorResponse(Response response,
                                       int expectedStatus) {
        Assert.assertNotNull(response);

        String entity = (String) response.getEntity();
        Assert.assertNotNull(entity);

        int status = response.getStatus();
        Assert.assertEquals(expectedStatus, status);
    }

    protected ServiceResource createServiceResource() throws Exception {
        ServiceResource resource = EasyMock
            .createMock("ServiceResource", ServiceResource.class);
        EasyMock.expect(resource.getAvailableServices()).andThrow(
            createRuntimeException()).anyTimes();
        EasyMock.expect(resource.getService(EasyMock.anyInt())).andThrow(
            createRuntimeException()).anyTimes();
        EasyMock.expect(resource.getDeployedService(EasyMock.anyInt(),
                                                    EasyMock.anyInt()))
            .andThrow(createRuntimeException())
            .anyTimes();
        EasyMock.expect(resource.getDeployedServiceProps(EasyMock.anyInt(),
                                                         EasyMock.anyInt()))
            .andThrow(createRuntimeException())
            .anyTimes();
        resource.undeployService(EasyMock.anyInt(), EasyMock.anyInt());
        EasyMock.expectLastCall().andThrow(createRuntimeException()).anyTimes();


        EasyMock.replay(resource);
        return resource;
    }

    protected RestUtil createRestUtil() throws Exception {
        RestUtil util = EasyMock.createMock("RestUtil", RestUtil.class);
        EasyMock.expect(util.getRequestContent(null, null)).andThrow(
            createRuntimeException()).anyTimes();

        EasyMock.replay(util);
        return util;
    }

    protected Throwable createRuntimeException() {
        Throwable t = null;
        try {
            t.toString();
        } catch (Exception e) {
            t = e;
        }
        Assert.assertNotNull(t);
        return t;
    }

}
