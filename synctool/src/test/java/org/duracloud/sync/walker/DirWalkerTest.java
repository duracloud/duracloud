/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.walker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.duracloud.sync.SyncTestBase;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: Mar 25, 2010
 */
public class DirWalkerTest extends SyncTestBase {

    @Test
    public void testDirWalker() {
        File tempDir = new File("target");
        List<File> dirs = new ArrayList<File>();
        dirs.add(tempDir);

        DirWalker dirWalker = new DirWalker(dirs);
        assertFalse(dirWalker.walkComplete());
        dirWalker.walkDirs();        
        assertTrue(dirWalker.walkComplete());

        int walkerFilesFound = 0;
        while(changedList.getChangedFile() != null) {
            walkerFilesFound++;
        }

        Collection tempDirFiles =
            FileUtils.listFiles(tempDir,
                                TrueFileFilter.INSTANCE,
                                TrueFileFilter.INSTANCE);

        assertEquals(walkerFilesFound, tempDirFiles.size());
        assertEquals(walkerFilesFound, dirWalker.getFilesCount());
    }
}
