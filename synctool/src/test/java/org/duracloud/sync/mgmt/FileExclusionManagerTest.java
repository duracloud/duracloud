/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: Daniel Bernstein
 * Date: July 27, 2017
 */
public class FileExclusionManagerTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testReadExcludeList() throws IOException {
        String toExclude = "file.txt\n*.log\ndir?";
        File excludeFile = new File("target", "excludelist.txt");
        FileUtils.writeStringToFile(excludeFile, toExclude);

        File testExcluded1 = new File("file.txt");
        File testExcluded2 = new File("dir0" + File.separator + "test.log");
        File testNotExcluded = new File("good.txt");

        FileExclusionManager fem = new FileExclusionManager();

        // Nothing should be excluded
        assertFalse(fem.isExcluded(testExcluded1));
        assertFalse(fem.isExcluded(testExcluded2));
        assertFalse(fem.isExcluded(testNotExcluded));

        // With exclusions
        fem = new FileExclusionManager(excludeFile);

        // Exclusions should be in effect
        assertTrue(fem.isExcluded(testExcluded1));
        assertTrue(fem.isExcluded(testExcluded2));
        assertFalse(fem.isExcluded(testNotExcluded));

        FileUtils.deleteQuietly(excludeFile);
    }

    @Test
    public void testExclude() {
        String fileByName = "reallybig.tiff";
        String dirByName = "logs";
        String fileInDir = ".DS_Store";
        String fileWildStar = "*.log";
        String fileWildQues = "file-dated-19??.txt";
        String dirWildStar = "files-*";

        // Test file by name (exclude list includes file name)
        assertTrue(testExcluded(fileByName, fileByName));

        // Test directory by name (exclude list includes dir name)
        String testPath = dirByName + File.separator + fileByName;
        assertTrue(testExcluded(testPath, dirByName));

        // Test file in directory (exclude list includes file found in dir)
        testPath = dirByName + File.separator + fileInDir;
        assertTrue(testExcluded(testPath, fileInDir));

        // Test file wildcard (exclude includes a file name with a wildcard)
        testPath = dirByName + File.separator + "test.log";
        assertTrue(testExcluded(testPath, fileWildStar));

        // Test file wildcard and that match is case insensitive
        testPath = dirByName + File.separator + "test.LOG";
        assertTrue(testExcluded(testPath, fileWildStar));

        // Test file wildcard (exclude includes a file name with a wildcard)
        testPath = dirByName + File.separator + "file-dated-1990.txt";
        assertTrue(testExcluded(testPath, fileWildQues));

        // Test dir wildcard (exclude includes a file name with a wildcard)
        testPath = "files-A1" + File.separator + fileByName;
        assertTrue(testExcluded(testPath, dirWildStar));

        // Test file and dir wildcard (exclude includes file and dir with a wildcard)
        testPath = dirByName + File.separator + "test.log";
        assertTrue(testExcluded(testPath, fileWildStar, dirWildStar));
        testPath = "files-A1" + File.separator + fileByName;
        assertTrue(testExcluded(testPath, fileWildStar, dirWildStar));

        // Test all rules together
        testPath = dirByName + File.separator + fileByName;
        assertTrue(testExcluded(testPath, fileByName, dirByName,
                                fileInDir, fileWildStar, fileWildQues,
                                dirWildStar));

        // Test non-match
        testPath = "dir1" + File.separator + "file1.txt";
        assertFalse(testExcluded(testPath, fileWildStar));
    }

    private boolean testExcluded(String test,
                                 String... rules) {

        FileExclusionManager fem = new FileExclusionManager(buildExcludeList(rules));
        return fem.isExcluded(new File(test));
    }

    private List<String> buildExcludeList(String... rules) {
        List<String> excludeList = new ArrayList<>();
        for (String rule : rules) {
            excludeList.add(rule);
        }
        return excludeList;
    }

}
