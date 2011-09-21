/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.rest.RestUtil;
import org.duracloud.durastore.util.StorageProviderFactory;
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

    private StorageProviderFactory storageProviderFactory;
    private RestUtil restUtil;
    private InitRest initRest;

    @Before
    public void setup() {
        storageProviderFactory =
            EasyMock.createMock(StorageProviderFactory.class);
        restUtil = EasyMock.createMock(RestUtil.class);
        initRest = new InitRest(storageProviderFactory, restUtil);
    }

    private void replayMocks() {
        EasyMock.replay(storageProviderFactory, restUtil);
    }

    @After
    public void teardown() {
        EasyMock.verify(storageProviderFactory, restUtil);
    }

    @Test
    public void testIsInitialized() {
        // Not initialized
        EasyMock.expect(storageProviderFactory.isInitialized())
            .andReturn(false)
            .times(1);

        // Initialized
        EasyMock.expect(storageProviderFactory.isInitialized())
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
