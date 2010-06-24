/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author: Bill Branan
 * Date: Mar 19, 2010
 */
public class DirectoryUtil {
    
    /*
     * Provides a listing of files in a directory, sorted in order
     * of most to least recent.
     */
    public static File[] listFilesSortedByModDate(File dir) {
        File[] backupDirFiles = dir.listFiles();
        Arrays.sort(backupDirFiles, new FileComparator());
        return backupDirFiles;
    }

    private static class FileComparator implements Comparator<File> {
      public int compare(File file1, File file2) {
        return FileUtils.isFileNewer(file1, file2) ? -1 : 1;
      }
    }
}
