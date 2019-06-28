/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.durastore.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpStatus;
import org.duracloud.common.util.WaitUtil;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.xml.StorageAccountsDocumentBinding;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String url = BaseRestTester.baseUrl + "/stores";
        HttpResponse response = BaseRestTester.restHelper.get(url);
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
        List<StorageProviderType> storageProviderList =
            Arrays.asList(StorageProviderType.values());
        assertTrue(storageProviderList.contains(primaryAcct.getType()));
    }

    @Test
    public void testStores() throws Exception {
        StorageAccountManager manager = createStorageAccountManager();
        Iterator<String> acctIds = manager.getStorageAccountIds();
        while (acctIds.hasNext()) {
            String acctId = acctIds.next();
            StorageProviderType type =
                manager.getStorageAccount(acctId).getType();
            // Test types
            if (!type.equals(StorageProviderType.TEST_RETRY) &&
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
        InputStream xmlStream =
            new ByteArrayInputStream(storesXML.getBytes("UTF-8"));
        StorageAccountsDocumentBinding binding =
            new StorageAccountsDocumentBinding();
        List<StorageAccount> accts =
            binding.createStorageAccountsFromXml(xmlStream);

        StorageAccountManager manager = new StorageAccountManager();
        manager.initialize(accts);
        assertNotNull(manager);
        assertNotNull(manager.getStorageAccountIds());
        return manager;
    }

    /**
     * Used to run the same set of tests over all configured storage providers
     */
    private void testStore(String acctId) throws Exception {
        // Add space1
        HttpResponse response = RestTestHelper.addSpace(BaseRestTester.spaceId, acctId);
        checkResponse(response, HttpStatus.SC_CREATED);

        // Add content1 to space1
        String url = BaseRestTester.baseUrl + "/" + BaseRestTester.spaceId + "/content1?storeID=" + acctId;
        response = BaseRestTester.restHelper.put(url, CONTENT, null);
        checkResponse(response, HttpStatus.SC_CREATED);

        // Delete content1 from space1
        url = BaseRestTester.baseUrl + "/" + BaseRestTester.spaceId + "/content1?storeID=" + acctId;
        response = BaseRestTester.restHelper.delete(url);
        String responseText = checkResponse(response, HttpStatus.SC_OK);
        assertNotNull(responseText);
        assertTrue(responseText.contains("content1"));
        assertTrue(responseText.contains("deleted"));

        // Delete space1
        WaitUtil.wait(10);
        response = RestTestHelper.deleteSpace(BaseRestTester.spaceId, acctId);
        checkResponse(response, HttpStatus.SC_OK);
    }
}