/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.util;

import static junit.framework.Assert.assertTrue;
import org.junit.Test;

import java.io.File;

/**
 * @author: Bill Branan
 * Date: Mar 19, 2010
 */
public class DirectoryUtilTest {

    @Test
    public void testDirectoryUtil() throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));

        File[] sortedDir = DirectoryUtil.listFilesSortedByModDate(tempDir);

        long prevFileMod = Long.MAX_VALUE;
        for(File file : sortedDir) {
            long fileMod = file.lastModified();
            assertTrue(fileMod <= prevFileMod);
            prevFileMod = fileMod;
        }
    }
}
