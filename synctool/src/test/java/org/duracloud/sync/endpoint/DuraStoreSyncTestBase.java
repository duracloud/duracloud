/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import static junit.framework.Assert.assertEquals;
import org.duracloud.sync.SyncIntegrationTestBase;
import org.junit.After;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        endpoint.syncFile(tempFile, tempDir);
        List<String> endpointContents = testEndpoint(endpoint, 1);
        assertEquals(tempFile.getName(), endpointContents.get(0));

        // Sync deleted file
        tempFile.delete();
        endpoint.syncFile(tempFile, tempDir);
        testEndpoint(endpoint, 0);
    }

    protected void testSyncNoDeletes(DuraStoreSyncEndpoint endpoint)
        throws Exception {
        // Space should be empty
        testEndpoint(endpoint, 0);

        // Sync new file
        File tempFile = File.createTempFile("temp", "file", tempDir);
        endpoint.syncFile(tempFile, tempDir);
        List<String> endpointContents = testEndpoint(endpoint, 1);
        assertEquals(tempFile.getName(), endpointContents.get(0));

        // Ensure file is not deleted at endpoint
        tempFile.delete();
        endpoint.syncFile(tempFile, tempDir);
        endpointContents = testEndpoint(endpoint, 1);
        assertEquals(tempFile.getName(), endpointContents.get(0));
    }

    protected List<String> testEndpoint(DuraStoreSyncEndpoint endpoint,
                                        int expectedSize)
        throws Exception {
        Thread.sleep(1000);
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

}
