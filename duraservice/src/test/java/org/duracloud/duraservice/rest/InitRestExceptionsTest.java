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
 * @author: Bill Branan
 * Date: 9/19/11
 */
public class InitRestExceptionsTest {

    private InitRest initRest;
    private ServiceResource serviceResource;
    private RestUtil restUtil;
    private RestExceptionsTestSupport support = new RestExceptionsTestSupport();

    @Before
    public void setUp() throws Exception {
        serviceResource = support.createServiceResource();
        restUtil = support.createRestUtil();
        initRest = new InitRest(serviceResource, restUtil);
    }

    @Test
    public void testInitializeServices() throws Exception {
        Response response = initRest.initializeServices();
        support.verifyErrorResponse(response);
    }

}
