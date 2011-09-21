/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.duracloud.common.rest.RestUtil;
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

    private RestExceptionsTestSupport support = new RestExceptionsTestSupport();

    @Before
    public void setUp() throws Exception {
        serviceResource = support.createServiceResource();
        restUtil = support.createRestUtil();
        serviceRest = new ServiceRest(serviceResource, restUtil);
    }

    @Test
    public void testGetServices() throws Exception {
        Response response = serviceRest.getServices(null);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testGetService() throws Exception {
        Response response = serviceRest.getService(-1);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testGetDeployedService() throws Exception {
        Response response = serviceRest.getDeployedService(-1, -1);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testGetDeployedServiceProperties() throws Exception {
        Response response = serviceRest.getDeployedServiceProperties(-1, -1);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testDeployService() throws Exception {
        Response response = serviceRest.deployService(-1, null);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testConfigureService() throws Exception {
        Response response = serviceRest.configureService(-1, -1);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testUndeployService() throws Exception {
        Response response = serviceRest.undeployService(-1, -1);
        support.verifyErrorResponse(response);
    }

}
