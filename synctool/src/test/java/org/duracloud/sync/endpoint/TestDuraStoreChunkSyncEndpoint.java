/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author: Bill Branan
 * Date: Apr 9, 2010
 */
public class TestDuraStoreChunkSyncEndpoint extends DuraStoreSyncTestBase {

    private static int maxFileSize = 1024;

    @Test
    public void testChunkSyncDeletesOn() throws Exception {
        DuraStoreChunkSyncEndpoint endpoint =
            new DuraStoreChunkSyncEndpoint(host,
                                           Integer.parseInt(port),
                                           context,
                                           getRootCredential().getUsername(),
                                           getRootCredential().getPassword(),
                                           spaceId,
                                           true,
                                           maxFileSize);
        testSync(endpoint);

        // Test chunking
        File largeFile = File.createTempFile("large", "file", tempDir);
        FileOutputStream fos = new FileOutputStream(largeFile);

        int filesize = maxFileSize + maxFileSize/2;
        for(int i=0;  i< filesize; i++) {
            fos.write(i);
        }
        fos.flush();
        fos.close();

        endpoint.syncFile(largeFile, tempDir);
        testEndpoint(endpoint, 3);
    }

    @Test
    public void testChunkSyncDeletesOff() throws Exception {
        DuraStoreChunkSyncEndpoint endpoint =
            new DuraStoreChunkSyncEndpoint(host,
                                           Integer.parseInt(port),
                                           context,
                                           getRootCredential().getUsername(),
                                           getRootCredential().getPassword(),
                                           spaceId,
                                           false,
                                           maxFileSize);
        testSyncNoDeletes(endpoint);
    }
}