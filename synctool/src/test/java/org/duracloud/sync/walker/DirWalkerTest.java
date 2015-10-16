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
import java.io.IOException;
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
        File checkDir = new File("src");
        List<File> dirs = new ArrayList<File>();
        dirs.add(checkDir);

        DirWalker dirWalker = new DirWalker(dirs, null);
        assertFalse(dirWalker.walkComplete());
        dirWalker.walkDirs();        
        assertTrue(dirWalker.walkComplete());

        int walkerFilesFound = 0;
        while(changedList.reserve() != null) {
            walkerFilesFound++;
        }

        Collection tempDirFiles =
            FileUtils.listFiles(checkDir,
                                TrueFileFilter.INSTANCE,
                                TrueFileFilter.INSTANCE);

        assertEquals(walkerFilesFound, tempDirFiles.size());
        assertEquals(walkerFilesFound, dirWalker.getFilesCount());
    }

    @Test
    public void testExclude() {
        String fileByName = "reallybig.tiff";
        String dirByName = "logs";
        String fileInDir = ".DS_Store";
        String fileWildStar = "*.log";
        String fileWildQues = "file-dated-19??.txt";
        String dirWildStar = "files-*";

        DirWalker dirWalker = new DirWalker(null, null);

        // Test file by name (exclude list includes file name)
        assertTrue(testExcluded(dirWalker, fileByName, fileByName));

        // Test directory by name (exclude list includes dir name)
        String testPath = dirByName + File.separator + fileByName;
        assertTrue(testExcluded(dirWalker, testPath, dirByName));

        // Test file in directory (exclude list includes file found in dir)
        testPath = dirByName + File.separator + fileInDir;
        assertTrue(testExcluded(dirWalker, testPath, fileInDir));

        // Test file wildcard (exclude includes a file name with a wildcard)
        testPath = dirByName + File.separator + "test.log";
        assertTrue(testExcluded(dirWalker, testPath, fileWildStar));

        // Test file wildcard and that match is case insensitive
        testPath = dirByName + File.separator + "test.LOG";
        assertTrue(testExcluded(dirWalker, testPath, fileWildStar));

        // Test file wildcard (exclude includes a file name with a wildcard)
        testPath = dirByName + File.separator + "file-dated-1990.txt";
        assertTrue(testExcluded(dirWalker, testPath, fileWildQues));

        // Test dir wildcard (exclude includes a file name with a wildcard)
        testPath = "files-A1" + File.separator + fileByName;
        assertTrue(testExcluded(dirWalker, testPath, dirWildStar));

        // Test file and dir wildcard (exclude includes file and dir with a wildcard)
        testPath = dirByName + File.separator + "test.log";
        assertTrue(testExcluded(dirWalker, testPath, fileWildStar, dirWildStar));
        testPath = "files-A1" + File.separator + fileByName;
        assertTrue(testExcluded(dirWalker, testPath, fileWildStar, dirWildStar));

        // Test all rules together
        testPath = dirByName + File.separator + fileByName;
        assertTrue(testExcluded(dirWalker, testPath, fileByName, dirByName,
                                fileInDir, fileWildStar, fileWildQues,
                                dirWildStar));

        // Test non-match
        testPath = "dir1" + File.separator + "file1.txt";
        assertFalse(testExcluded(dirWalker, testPath, fileWildStar));
    }

    private boolean testExcluded(DirWalker dirWalker,
                                 String test,
                                 String... rules) {
        dirWalker.setExcludeList(buildExcludeList(rules));
        return dirWalker.excluded(new File(test));
    }

    private List<String> buildExcludeList(String... rules) {
        List<String> excludeList = new ArrayList<>();
        for(String rule : rules) {
            excludeList.add(rule);
        }
        return excludeList;
    }

    @Test
    public void testReadExcludeList() throws IOException {
        String toExclude = "file.txt\n*.log\ndir?";
        File excludeFile = new File("target", "excludelist.txt");
        FileUtils.writeStringToFile(excludeFile, toExclude);

        File testExcluded1 = new File("file.txt");
        File testExcluded2 = new File("dir0" + File.separator + "test.log");
        File testNotExcluded = new File("good.txt");

        // Create DirWalker with no exclusions
        DirWalker dirWalker = new DirWalker(null, null);

        // Nothing should be excluded
        assertFalse(dirWalker.excluded(testExcluded1));
        assertFalse(dirWalker.excluded(testExcluded2));
        assertFalse(dirWalker.excluded(testNotExcluded));

        // Create DirWalker with exclusions
        dirWalker = new DirWalker(null, excludeFile);

        // Exclusions should be in effect
        assertTrue(dirWalker.excluded(testExcluded1));
        assertTrue(dirWalker.excluded(testExcluded2));
        assertFalse(dirWalker.excluded(testNotExcluded));

        FileUtils.deleteQuietly(excludeFile);
    }

}
