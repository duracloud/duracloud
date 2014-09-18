/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.duracloud.client.ContentStore;
import org.duracloud.client.util.StoreClientUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.sync.SyncIntegrationTestBase;
import org.junit.After;
import org.junit.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * @author: Bill Branan
 * Date: Apr 9, 2010
 */
public class DuraStoreSyncTestBase extends SyncIntegrationTestBase {

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        // Clean up space
        Iterator<String> contents = store.getSpaceContents(spaceId);
        while(contents.hasNext()) {
            store.deleteContent(spaceId, contents.next());            
        }
    }

    protected void testSync(DuraStoreSyncEndpoint endpoint)
        throws Exception {
        // Space should be empty
        testEndpoint(endpoint, 0);

        // Sync new file
        File tempFile = File.createTempFile("temp", "file", tempDir);
        MonitoredFile monitoredFile = new MonitoredFile(tempFile);
        endpoint.syncFile(monitoredFile, tempDir);
        List<String> endpointContents = testEndpoint(endpoint, 1);
        assertEquals(tempFile.getName(), endpointContents.get(0));

        // Sync deleted file
        tempFile.delete();
        endpoint.syncFile(monitoredFile, tempDir);
        testEndpoint(endpoint, 0);
    }

    protected void testSyncNoDeletes(DuraStoreSyncEndpoint endpoint)
        throws Exception {
        // Space should be empty
        testEndpoint(endpoint, 0);

        // Sync new file
        File tempFile = File.createTempFile("temp", "file", tempDir);
        MonitoredFile monitoredFile = new MonitoredFile(tempFile);
        endpoint.syncFile(monitoredFile, tempDir);
        List<String> endpointContents = testEndpoint(endpoint, 1);
        assertEquals(tempFile.getName(), endpointContents.get(0));

        // Ensure file is not deleted at endpoint
        tempFile.delete();
        endpoint.syncFile(monitoredFile, tempDir);
        endpointContents = testEndpoint(endpoint, 1);
        assertEquals(tempFile.getName(), endpointContents.get(0));
    }

    protected List<String> testEndpoint(DuraStoreSyncEndpoint endpoint,
                                        int expectedSize)
        throws Exception {
        waitForSpaceToBeCreated(spaceId);
        
        List<String> spaceContents =
            iteratorToList(store.getSpaceContents(spaceId));
        assertEquals(expectedSize, spaceContents.size());

        List<String> endpointContents =
            iteratorToList(endpoint.getFilesList());
        assertEquals(expectedSize, endpointContents.size());

        assertEquals(spaceContents, endpointContents);
        return endpointContents;
    }

    protected List<String> iteratorToList(Iterator<String> it) {
        List<String> list = new ArrayList<String>();
        while(it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    private void waitForSpaceToBeCreated(String spaceId)
        throws ContentStoreException {
        int maxTries = 10;
        int numTries = 0;
        long millis = 500;

        Map<String, String> properties = null;
        while (numTries < maxTries) {
            properties = getProperties(spaceId);
            if (properties != null && properties.size() > 0) {
                return;
            }
            numTries++;
            sleep(millis);
        }

        Assert.fail("Space was never created: " + spaceId);
    }

    private Map<String, String> getProperties(String spaceId){
        Map<String, String> properties = null;
        try {
            properties = store.getSpaceProperties(spaceId);
        } catch (ContentStoreException e) {
            // do nothing.
        }
        return properties;
    }

    protected ContentStore getContentStore() throws Exception {
        StoreClientUtil storeUtil = new StoreClientUtil();
        return storeUtil.createContentStore(SyncIntegrationTestBase.host,
                                            Integer.parseInt(
                                                SyncIntegrationTestBase.port),
                                            SyncIntegrationTestBase.context,
                                            SyncIntegrationTestBase.
                                                getRootCredential().
                                                getUsername(),
                                            SyncIntegrationTestBase.
                                                getRootCredential().
                                                getPassword(),
                                            null);
    }    

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing.
        }
    }

}
