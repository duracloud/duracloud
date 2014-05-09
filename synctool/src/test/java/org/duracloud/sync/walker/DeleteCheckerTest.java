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
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * @author: Bill Branan
 * Date: Mar 29, 2010
 */
public class DeleteCheckerTest extends SyncTestBase {

    private File tempDir;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        tempDir = createTempDir("delete-check");
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testDeleteChecker() throws Exception {
        File tempFile = File.createTempFile("temp", "file", tempDir);
        String delFile = "deletedFile";

        List<String> filesList = new ArrayList<>();
        filesList.add(tempFile.getName());
        filesList.add(delFile);

        List<File> syncDirs = new ArrayList<>();
        syncDirs.add(tempDir);

        DeleteChecker deleteChecker =
            new DeleteChecker(filesList.iterator(), syncDirs, null);
        deleteChecker.run();

        ChangedFile changedFile = changedList.getChangedFile();
        assertNotNull(changedFile);
        assertEquals(delFile, changedFile.getFile().getName());
        assertNull(changedList.getChangedFile());
    }

    @Test
    public void testDeleteCheckerPrefix() throws Exception {
        File tempFile = File.createTempFile("temp", "file", tempDir);
        String delFile = "deletedFile";

        String prefix = "prefix/";

        List<String> filesList = new ArrayList<>();
        filesList.add(prefix + tempFile.getName());
        filesList.add(delFile);

        List<File> syncDirs = new ArrayList<>();
        syncDirs.add(tempDir);

        DeleteChecker deleteChecker =
            new DeleteChecker(filesList.iterator(), syncDirs, prefix);
        deleteChecker.run();

        ChangedFile changedFile = changedList.getChangedFile();
        assertNotNull(changedFile);
        assertEquals(delFile, changedFile.getFile().getName());
        assertNull(changedList.getChangedFile());
    }

}
