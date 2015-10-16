/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.walker;

import org.apache.commons.io.FileUtils;
import org.duracloud.sync.SyncTestBase;
import org.duracloud.sync.mgmt.ChangedFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: Apr 9, 2010
 */
public class RestartDirWalkerTest extends SyncTestBase {

    private File tempDir;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        tempDir = createTempDir("restart-dir");
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testRestartDirWalker() throws Exception {
        List<File> dirs = new ArrayList<File>();
        dirs.add(tempDir);

        // Create three sub directories with files
        File subDir1 = new File(tempDir, "subdir1");
        subDir1.mkdir();
        File sub1file1 = File.createTempFile("subdir1", "file1", subDir1);
        File sub1file2 = File.createTempFile("subdir1", "file2", subDir1);

        File subDir2 = new File(tempDir, "subdir2");
        subDir2.mkdir();
        File sub2file1 = File.createTempFile("subdir2", "file1", subDir2);

        File subDir3 = new File(tempDir, "subdir3");
        subDir3.mkdir();
        File sub3file1 = File.createTempFile("subdir3", "file1", subDir3);

        long restartTime = System.currentTimeMillis();
        Thread.sleep(1000);

        // Add a file to subdir 2
        File sub2file2 = File.createTempFile("subdir2", "file2", subDir2);
        // Update file in subdir 1
        FileUtils.touch(sub1file1);

        // Run the restart walker
        RestartDirWalker rDirWalker =
            new RestartDirWalker(dirs, null, restartTime);
        assertFalse(rDirWalker.walkComplete());
        rDirWalker.walkDirs();
        assertTrue(rDirWalker.walkComplete());

        // Changed list should include sub1file1, and all files from sub2
        List<File> changedFiles = new ArrayList<File>();
        ChangedFile changedFile = changedList.reserve();
        while(changedFile != null) {
            changedFiles.add(changedFile.getFile());
            changedFile = changedList.reserve();
        }

        assertEquals(3, changedFiles.size());
        for(File file : changedFiles) {
            assertTrue(file.equals(sub1file1) ||
                       file.equals(sub2file1) ||
                       file.equals(sub2file2));
        }

        assertNull(changedList.reserve());
    }
}