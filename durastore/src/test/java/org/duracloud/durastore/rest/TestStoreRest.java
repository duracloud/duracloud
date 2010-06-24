/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Runtime test of store REST API. The durastore web application must be
 * deployed and available at the baseUrl location in order for these tests to
 * pass.
 *
 * @author Bill Branan
 */
public class TestStoreRest extends BaseRestTester {

    protected static final Logger log =
        LoggerFactory.getLogger(TestStoreRest.class);

    private static final String CONTENT = "<content />";

    private String storesXML;

    @Before
    public void setUp() throws Exception {
        // Retrieve the stores listing
        setNewSpaceId();
        String url = baseUrl + "/stores";
        HttpResponse response = restHelper.get(url);
        storesXML = checkResponse(response, HttpStatus.SC_OK);
        assertNotNull(storesXML);
        assertTrue(storesXML.contains("<storageProviderAccounts>"));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetStores() throws Exception {
        StorageAccountManager manager = createStorageAccountManager();
        StorageAccount primaryAcct = manager.getPrimaryStorageAccount();
        assertNotNull(primaryAcct);
        assertEquals(StorageProviderType.AMAZON_S3, primaryAcct.getType());
    }

    @Test
    public void testStores() throws Exception {
        StorageAccountManager manager = createStorageAccountManager();
        Iterator<String> acctIds = manager.getStorageAccountIds();
        while(acctIds.hasNext()) {
            String acctId = acctIds.next();
            StorageProviderType type =
                manager.getStorageAccount(acctId).getType();
            if(// Test types
               !type.equals(StorageProviderType.TEST_RETRY) &&
               !type.equals(StorageProviderType.TEST_VERIFY_CREATE) &&
               !type.equals(StorageProviderType.TEST_VERIFY_DELETE)) {
                log.info("Testing storage account with id " +
                         acctId + " and type " + type.name());
                testStore(acctId);
            }
        }
    }

    private StorageAccountManager createStorageAccountManager()
            throws Exception {
        InputStream is = new ByteArrayInputStream(storesXML.getBytes());
        StorageAccountManager manager = new StorageAccountManager(is, true);
        assertNotNull(manager);
        assertNotNull(manager.getStorageAccountIds());
        return manager;
    }

    /**
     * Used to run the same set of tests over all configured storage providers
     */
    private void testStore(String acctId) throws Exception {
        // Add space1
        HttpResponse response = RestTestHelper.addSpace(spaceId, acctId);
        checkResponse(response, HttpStatus.SC_CREATED);

        // Add content1 to space1
        String url = baseUrl + "/" + spaceId + "/content1?storeID=" + acctId;
        response = restHelper.put(url, CONTENT, null);
        checkResponse(response, HttpStatus.SC_CREATED);

        // Delete content1 from space1
        url = baseUrl + "/" + spaceId + "/content1?storeID=" + acctId;
        response = restHelper.delete(url);
        String responseText = checkResponse(response, HttpStatus.SC_OK);
        assertNotNull(responseText);
        assertTrue(responseText.contains("content1"));
        assertTrue(responseText.contains("deleted"));

        // Delete space1
        response = RestTestHelper.deleteSpace(spaceId, acctId);
        checkResponse(response, HttpStatus.SC_OK);
    }
}