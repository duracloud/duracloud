/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import org.apache.commons.io.FileUtils;
import org.duracloud.sync.SyncTestBase;
import org.junit.Test;

import java.io.File;

/**
 * @author: Bill Branan
 * Date: Mar 26, 2010
 */
public class FileSystemSyncEndpointTest extends SyncTestBase {

    @Test
    public void testGetSyncFile() throws Exception {
        File syncToDir = new File("/a/b/c");
        FileSystemSyncEndpoint syncEndpoint =
            new FileSystemSyncEndpoint(syncToDir, true);

        File watchDir = new File("/a/b/d/e");
        File syncFile = new File("/a/b/d/e/f/g.txt");
        File syncToFile = syncEndpoint.getSyncToFile(syncFile, watchDir);

        assertEquals(new File("/a/b/c/f/g.txt").getAbsolutePath(),
                     syncToFile.getAbsolutePath());
    }

    @Test
    public void testSyncToFileSystem() throws Exception {
        File syncToDir = createTempDir("syncToDir");
        FileSystemSyncEndpoint syncEndpoint =
            new FileSystemSyncEndpoint(syncToDir, true);

        File watchDir = createTempDir("watchDir");
        File syncFile = File.createTempFile("sync", "file", watchDir);
        assertTrue(syncFile.exists());

        File syncToFile = new File(syncToDir, syncFile.getName());
        assertFalse(syncToFile.exists());
        syncEndpoint.syncFile(syncFile, watchDir);
        assertTrue(syncToFile.exists());        

        FileUtils.deleteDirectory(syncToDir);
        FileUtils.deleteDirectory(watchDir);
    }
}
