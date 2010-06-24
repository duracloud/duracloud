/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import static junit.framework.Assert.assertEquals;
import org.duracloud.sync.SyncTestBase;
import org.duracloud.sync.endpoint.SyncEndpoint;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: Mar 25, 2010
 */
public class SyncManagerTest extends SyncTestBase {

    private int handledFiles;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        handledFiles = 0;
    }

    @Test
    public void testSyncManager() throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        List<File> watchDirs = new ArrayList<File>();
        watchDirs.add(tempDir);

        SyncManager syncManager =
            new SyncManager(watchDirs, new TestEndpoint(), 2, 100);
        syncManager.beginSync();

        int changedFiles = 10;
        for(int i=0; i < changedFiles; i++) {
            changedList.addChangedFile(new File(tempDir, "test-file-" + i));
        }
        Thread.sleep(200);
        assertEquals(changedFiles, handledFiles);

        syncManager.endSync();
    }

    private class TestEndpoint implements SyncEndpoint {
        public boolean syncFile(File file, File watchDir) {
            handledFiles++;
            return true;
        }

        public Iterator<String> getFilesList() {
            return null;
        }
    }

    @Test
    public void testGetWatchDir() throws Exception {
        File tempDir1 = new File("/a/b");
        File tempDir2 = new File("/a/c");
        List<File> watchDirs = new ArrayList<File>();
        watchDirs.add(tempDir1);
        watchDirs.add(tempDir2);

        SyncManager syncManager =
            new SyncManager(watchDirs, new TestEndpoint(), 2, 100);

        assertEquals(tempDir1,
                     syncManager.getWatchDir(new File("/a/b/file.txt")));
        assertEquals(tempDir2,
                     syncManager.getWatchDir(new File("/a/c/file.txt")));
        assertEquals(tempDir1,
                     syncManager.getWatchDir(new File("/a/b/q/r/file.txt")));
        assertEquals(tempDir2,
                     syncManager.getWatchDir(new File("/a/c/t/u/file.txt")));
    }

}
