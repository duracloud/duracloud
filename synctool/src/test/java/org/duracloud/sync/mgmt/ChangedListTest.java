/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import org.duracloud.sync.SyncTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;


/**
 * @author: Bill Branan
 * Date: Mar 19, 2010
 */
public class ChangedListTest extends SyncTestBase {

    private File changedFile;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        changedFile = File.createTempFile("changed", "file");
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        changedFile.delete();
    }

    @Test
    public void testChangedList() throws Exception {
        long version = changedList.getVersion();
        changedList.addChangedFile(changedFile);
        assertEquals(version + 1, changedList.getVersion());

        ChangedFile retrievedFile = changedList.getChangedFile();
        assertEquals(changedFile.getAbsolutePath(),
                     retrievedFile.getFile().getAbsolutePath());
        assertEquals(version + 2, changedList.getVersion());        
    }

    @Test
    public void testChangedListPersist() throws Exception {
        changedList.addChangedFile(changedFile);

        File persistFile = File.createTempFile("persist", "file");
        changedList.persist(persistFile);

        ChangedFile retrievedFile = changedList.getChangedFile();
        assertEquals(changedFile.getAbsolutePath(),
                     retrievedFile.getFile().getAbsolutePath());
        assertNull(changedList.getChangedFile());

        changedList.restore(persistFile);

        retrievedFile = changedList.getChangedFile();
        assertEquals(changedFile.getAbsolutePath(),
                     retrievedFile.getFile().getAbsolutePath());
        assertNull(changedList.getChangedFile());

        persistFile.delete();
    }
}
