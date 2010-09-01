/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.duracloud.common.rest.RestUtil;
import org.duracloud.duraservice.error.NoSuchDeployedServiceException;
import org.duracloud.duraservice.error.NoSuchServiceException;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 * This class tests top-level error handling of ServiceRest.
 *
 * @author Andrew Woods
 *         Date: Aug 31, 2010
 */
public class ServiceRestExceptionsTest {

    private ServiceRest serviceRest;
    private ServiceResource serviceResource;
    private RestUtil restUtil;

    @Before
    public void setUp() throws Exception {
        serviceResource = createServiceResource();
        restUtil = createRestUtil();
        serviceRest = new ServiceRest(serviceResource, restUtil);
    }

    @Test
    public void testInitializeServices() throws Exception {
        Response response = serviceRest.initializeServices();
        verifyErrorResponse(response);
    }

    @Test
    public void testGetServices() throws Exception {
        Response response = serviceRest.getServices(null);
        verifyErrorResponse(response);
    }

    @Test
    public void testGetService() throws Exception {
        Response response = serviceRest.getService(-1);
        verifyErrorResponse(response);
    }

    @Test
    public void testGetDeployedService() throws Exception {
        Response response = serviceRest.getDeployedService(-1, -1);
        verifyErrorResponse(response);
    }

    @Test
    public void testGetDeployedServiceProperties() throws Exception {
        Response response = serviceRest.getDeployedServiceProperties(-1, -1);
        verifyErrorResponse(response);
    }

    @Test
    public void testDeployService() throws Exception {
        Response response = serviceRest.deployService(-1, null);
        verifyErrorResponse(response);
    }

    @Test
    public void testConfigureService() throws Exception {
        Response response = serviceRest.configureService(-1, -1);
        verifyErrorResponse(response);
    }

    @Test
    public void testUndeployService() throws Exception {
        Response response = serviceRest.undeployService(-1, -1);
        verifyErrorResponse(response);
    }

    private void verifyErrorResponse(Response response) {
        Assert.assertNotNull(response);

        String entity = (String) response.getEntity();
        Assert.assertNotNull(entity);

        int status = response.getStatus();
        int expectedStatus = Response.Status
            .INTERNAL_SERVER_ERROR
            .getStatusCode();
        Assert.assertEquals(expectedStatus, status);
    }

    private ServiceResource createServiceResource() throws Exception {
        ServiceResource resource = EasyMock.createMock("ServiceResource",
                                                       ServiceResource.class);
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

    private RestUtil createRestUtil() throws Exception {
        RestUtil util = EasyMock.createMock("RestUtil", RestUtil.class);
        EasyMock.expect(util.getRequestContent(null, null)).andThrow(
            createRuntimeException()).anyTimes();

        EasyMock.replay(util);
        return util;
    }

    private Throwable createRuntimeException() {
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
