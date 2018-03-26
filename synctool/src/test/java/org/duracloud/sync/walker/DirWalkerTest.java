/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.walker;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.duracloud.sync.SyncTestBase;
import org.duracloud.sync.mgmt.ChangedList;
import org.duracloud.sync.mgmt.FileExclusionManager;
import org.easymock.Mock;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: Mar 25, 2010
 */
public class DirWalkerTest extends SyncTestBase {

    @Mock
    private FileExclusionManager fileExclusionManager;

    @Test
    public void testDirWalker() {
        fileExclusionManager = createMock(FileExclusionManager.class);
        expect(fileExclusionManager.isExcluded(isA(File.class))).andReturn(false).times(2, Integer.MAX_VALUE);
        File checkDir = new File("src");
        List<File> dirs = new ArrayList<File>();
        dirs.add(checkDir);

        replayAll();
        DirWalker dirWalker = createDirWalker(dirs);

        int walkerFilesFound = 0;
        while (changedList.reserve() != null) {
            walkerFilesFound++;
        }

        Collection<File> tempDirFiles = listFiles(checkDir);

        assertEquals(walkerFilesFound, tempDirFiles.size());
        assertEquals(walkerFilesFound, dirWalker.getFilesCount());
    }

    @Test
    public void testDirWalkerWithExclusions() {
        fileExclusionManager = createMock(FileExclusionManager.class);
        expect(fileExclusionManager.isExcluded(isA(File.class))).andReturn(true)
                                                                .times(1);
        File checkDir = new File("src");
        List<File> dirs = new ArrayList<File>();
        dirs.add(checkDir);

        replayAll();
        DirWalker dirWalker = createDirWalker(dirs);

        Collection<File> tempDirFiles = listFiles(checkDir);

        assertTrue(tempDirFiles.size() > 1);
        assertEquals(0, ChangedList.getInstance().getListSize());
        assertEquals(0, dirWalker.getFilesCount());
    }

    protected DirWalker createDirWalker(List<File> dirs) {
        DirWalker dirWalker = new DirWalker(dirs, fileExclusionManager);
        assertFalse(dirWalker.walkComplete());
        dirWalker.walkDirs();
        assertTrue(dirWalker.walkComplete());
        return dirWalker;
    }

    protected Collection<File> listFiles(File checkDir) {
        Collection<File> tempDirFiles =
            FileUtils.listFiles(checkDir,
                                TrueFileFilter.INSTANCE,
                                TrueFileFilter.INSTANCE);
        return tempDirFiles;
    }

}
