/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.storage.util.StorageProviderFactory;
import org.duracloud.storage.xml.StorageAccountsDocumentBinding;
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
    private StorageAccountsDocumentBinding documentBinding;

    private RestExceptionsTestSupport support = new RestExceptionsTestSupport();

    @Before
    public void setUp() throws Exception {
        storageProviderFactory = support.createStorageProviderFactory();
        documentBinding = new StorageAccountsDocumentBinding();
        storeRest = new StoreRest(storageProviderFactory, documentBinding);
    }

    @Test
    public void testGetStores() throws Exception {
        Response response = storeRest.getStores();
        support.verifyErrorResponse(response);
    }

}
