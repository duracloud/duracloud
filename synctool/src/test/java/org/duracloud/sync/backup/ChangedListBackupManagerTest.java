/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.backup;

import static junit.framework.Assert.*;

import java.io.File;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.duracloud.sync.SyncTestBase;
import org.duracloud.sync.mgmt.ChangedFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: Mar 25, 2010
 */
public class ChangedListBackupManagerTest  extends SyncTestBase {

    private File tempDir;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        tempDir = createTempDir("backup-dir");
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testChangedListBackupManager() throws Exception {
        long backupFrequency = 100;
        ChangedListBackupManager bkMan =
            new ChangedListBackupManager(changedList, tempDir, backupFrequency, new LinkedList<File>());
        new Thread(bkMan).start();

        String testFileName =  "testfile" + System.currentTimeMillis();
        File file = new File(tempDir, testFileName);
        file.createNewFile();
        file.deleteOnExit();
        changedList.addChangedFile(file);

        Thread.sleep(backupFrequency * 3);
        bkMan.endBackup();

        ChangedFile changedFile = changedList.reserve();
        changedFile.remove();
        assertNotNull(changedFile);
        assertEquals(testFileName, changedFile.getFile().getName());
        assertNull(changedList.reserve());

        bkMan.loadBackup();

        changedFile = changedList.reserve();
        assertNotNull(changedFile);
        assertEquals(testFileName, changedFile.getFile().getName());
        assertNull(changedList.reserve());        
    }
}
