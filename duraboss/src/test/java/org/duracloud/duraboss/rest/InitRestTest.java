/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest;

import org.duracloud.common.rest.RestUtil;
import org.easymock.classextension.EasyMock;
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

    private StorageReportResource storageResource;
    private ServiceReportResource serviceResource;
    private RestUtil restUtil;
    private InitRest initRest;

    @Before
    public void setup() {
        storageResource = EasyMock.createMock(StorageReportResource.class);
        serviceResource = EasyMock.createMock(ServiceReportResource.class);
        restUtil = EasyMock.createMock(RestUtil.class);
        initRest = new InitRest(storageResource, serviceResource,
                                null, null, restUtil, null, null);
    }

    private void replayMocks() {
        EasyMock.replay(storageResource, serviceResource, restUtil);
    }

    @After
    public void teardown() {
        EasyMock.verify(storageResource, serviceResource, restUtil);
    }

    @Test
    public void testIsInitialized() {
        // Not initialized
        EasyMock.expect(storageResource.isInitialized())
            .andReturn(false)
            .times(1);

        // Initialized
        EasyMock.expect(storageResource.isInitialized())
            .andReturn(true)
            .times(1);

        EasyMock.expect(serviceResource.isInitialized())
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
