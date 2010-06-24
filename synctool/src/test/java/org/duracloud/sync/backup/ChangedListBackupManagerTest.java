/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.backup;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertEquals;
import org.apache.commons.io.FileUtils;
import org.duracloud.sync.SyncTestBase;
import org.duracloud.sync.mgmt.ChangedFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

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
        ChangedListBackupManager bkMan =
            new ChangedListBackupManager(changedList, tempDir, 100);
        new Thread(bkMan).start();
        String testFileName = "testfile";
        changedList.addChangedFile(new File(testFileName));
        Thread.sleep(800);
        bkMan.endBackup();

        ChangedFile changedFile = changedList.getChangedFile();
        assertNotNull(changedFile);
        assertEquals(testFileName, changedFile.getFile().getName());
        assertNull(changedList.getChangedFile());

        bkMan.loadBackup();

        changedFile = changedList.getChangedFile();
        assertNotNull(changedFile);
        assertEquals(testFileName, changedFile.getFile().getName());
        assertNull(changedList.getChangedFile());        
    }
}
