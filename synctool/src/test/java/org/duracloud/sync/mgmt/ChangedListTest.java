/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import static junit.framework.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.duracloud.sync.SyncTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


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
        changedList.clear();
        changedFile.delete();
    }

    @Test
    public void testChangedList() throws Exception {
        long version = changedList.getVersion();
        changedList.addChangedFile(changedFile);
        assertEquals(version + 1, changedList.getVersion());

        ChangedFile retrievedFile = changedList.reserve();
        assertEquals(changedFile.getAbsolutePath(),
                     retrievedFile.getFile().getAbsolutePath());
        assertEquals(version + 2, changedList.getVersion());        
    }

    @Test
    public void testChangedListPersist() throws Exception {
        changedList.addChangedFile(changedFile);

        File persistFile = File.createTempFile("persist", "file");
        changedList.persist(persistFile);

        ChangedFile retrievedFile = changedList.reserve();
        
        assertEquals(changedFile.getAbsolutePath(),
                     retrievedFile.getFile().getAbsolutePath());
        assertNull(changedList.reserve());

        changedList.restore(persistFile, new ArrayList<File>());

        retrievedFile = changedList.reserve();
        assertEquals(changedFile.getAbsolutePath(),
                     retrievedFile.getFile().getAbsolutePath());
        assertNull(changedList.reserve());

        persistFile.delete();
    }

    @Test
    public void testReserveRestoreBeforeRemove() throws Exception {
        changedList.addChangedFile(changedFile);
        assertEquals(1, changedList.getListSize());
        changedList.reserve();
        File persistFile = File.createTempFile("persist", "file");
        changedList.persist(persistFile);
        changedList.restore(persistFile, new ArrayList<File>());
        assertEquals(1, changedList.getListSize());
        persistFile.delete();
    }

    @Test
    public void testReserveRestoreAfterRemove() throws Exception {
        changedList.addChangedFile(changedFile);
        assertEquals(1, changedList.getListSize());
        ChangedFile reserved = changedList.reserve();
        reserved.remove();
        File persistFile = File.createTempFile("persist", "file");
        changedList.persist(persistFile);
        changedList.restore(persistFile, new ArrayList<File>());
        assertEquals(0, changedList.getListSize());
        persistFile.delete();
    }
    
    @Test
    public void testReserveUnreserve() throws Exception {
        changedList.addChangedFile(changedFile);
        assertEquals(1, changedList.getListSize());
        ChangedFile reserved = changedList.reserve();
        assertEquals(0, changedList.getListSize());
        reserved.unreserve();
        assertEquals(1, changedList.getListSize());
    }

    @Test
    public void testChangedListContainsFilesThatDoNotMatchContentDirs() throws Exception {
        changedList.addChangedFile(changedFile);
        File contentDir = new File(System.getProperty("java.io.tmp"), System.currentTimeMillis()+"");
        assertTrue(contentDir.mkdir());
        contentDir.deleteOnExit();
        File persistFile = File.createTempFile("persist", "file");
        changedList.persist(persistFile);

        ChangedFile retrievedFile = changedList.reserve();
        assertEquals(changedFile.getAbsolutePath(),
                     retrievedFile.getFile().getAbsolutePath());
        assertNull(changedList.reserve());

        changedList.restore(persistFile, Arrays.asList(new File[]{contentDir}));

        retrievedFile = changedList.reserve();
        assertNull(changedList.reserve());

        persistFile.delete();
    }

    @Test
    public void testPeek() throws Exception {
        int fileCount = 100;
        List<File> files = new ArrayList<File>(fileCount);
        for(int i = 0; i < fileCount; i++){
            File f = new File("changedListTest-"+i+".tmp");
            files.add(f);
            changedList.addChangedFile(f);
        }
        
        Assert.assertEquals(fileCount, changedList.getListSize());
        
        List<File> peekedFiles = changedList.peek(fileCount-1);
        
        for(int i = 0; i < peekedFiles.size(); i++){
            Assert.assertEquals(files.get(i), peekedFiles.get(i));
        }
        
    }

    @Test
    public void testClear() throws Exception {
        int fileCount = 5;
        List<File> files = new ArrayList<File>(fileCount);
        for(int i = 0; i < fileCount; i++){
            File f = new File("changedListTest-"+i+".tmp");
            files.add(f);
            changedList.addChangedFile(f);
        }
        
        Assert.assertEquals(fileCount, changedList.getListSize());
        
        changedList.clear();
        Assert.assertEquals(0, changedList.getListSize());
    }

}
