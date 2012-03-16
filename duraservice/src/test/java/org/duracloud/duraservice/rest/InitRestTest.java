/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.duracloud.common.rest.RestUtil;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;


/**
 * @author: Bill Branan
 * Date: 9/21/11
 */
public class InitRestTest {

    private ServiceResource serviceResource;
    private RestUtil restUtil;
    private InitRest initRest;

    @Before
    public void setup() {
        serviceResource = EasyMock.createMock(ServiceResource.class);
        restUtil = EasyMock.createMock(RestUtil.class);
        initRest = new InitRest(serviceResource, restUtil);
    }

    private void replayMocks() {
        EasyMock.replay(serviceResource, restUtil);
    }

    @After
    public void teardown() {
        EasyMock.verify(serviceResource, restUtil);
    }

    @Test
    public void testIsInitialized() {
        // Not initialized
        EasyMock.expect(serviceResource.isConfigured())
            .andReturn(false)
            .times(1);

        // Initialized
        EasyMock.expect(serviceResource.isConfigured())
            .andReturn(true)
            .times(1);

        replayMocks();

        // Not initialized
        Response response = initRest.isInitialized();
        assertEquals(503, response.getStatus());

        // Initialized
        response = initRest.isInitialized();
        assertEquals(200, response.getStatus());
    }

}
