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
import org.duracloud.durastore.util.TaskProviderFactory;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 * This class tests top-level error handling of StoreRest.
 *
 * @author Andrew Woods
 *         Date: Aug 31, 2010
 */
public class StoreRestExceptionsTest {

    private StoreRest storeRest;
    private StorageProviderFactory storageProviderFactory;
    private RestUtil restUtil;

    private RestExceptionsTestSupport support = new RestExceptionsTestSupport();

    @Before
    public void setUp() throws Exception {
        storageProviderFactory = support.createStorageProviderFactory();
        restUtil = support.createRestUtil();
        storeRest = new StoreRest(storageProviderFactory, restUtil);
    }

    @Test
    public void testInitializeStores() throws Exception {
        Response response = storeRest.initializeStores();
        support.verifyErrorResponse(response);
    }

    @Test
    public void testGetStores() throws Exception {
        Response response = storeRest.getStores();
        support.verifyErrorResponse(response);
    }

}
