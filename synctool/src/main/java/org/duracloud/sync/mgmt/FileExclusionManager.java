/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a mechanism for clients to determine whether or not
 * a particular file should be ignored.
 *
 * @author Daniel Bernstein
 * @since July 27, 2017
 */
public class FileExclusionManager {
    private static Logger log = LoggerFactory.getLogger(FileExclusionManager.class);

    private WildcardFileFilter fileFilter;

    public FileExclusionManager(File excludeFile) {
        if (excludeFile == null) {
            throw new IllegalArgumentException("excludedFile must be non-null");
        }
        List<String> excludeList = readExcludeFile(excludeFile);
        setExcludeList(excludeList);
    }

    public FileExclusionManager() {
        setExcludeList(new LinkedList<>());
    }

    public FileExclusionManager(List<String> excludeList) {
        setExcludeList(excludeList);
    }

    private void setExcludeList(List<String> excludeList) {
        fileFilter = new WildcardFileFilter(excludeList, IOCase.INSENSITIVE);
    }

    private List<String> readExcludeFile(File excludeFile) {
        List<String> excludeList = new ArrayList<>();
        try (BufferedReader excludeReader =
                 new BufferedReader(new FileReader(excludeFile))) {
            String excludeItem = excludeReader.readLine();
            while (excludeItem != null) {
                String excludedItemTrimmed = excludeItem.trim();
                excludeList.add(excludedItemTrimmed);
                log.info("Added rule from exclude list: {}", excludedItemTrimmed);
                excludeItem = excludeReader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read exclude file " +
                                       excludeFile.getAbsolutePath() +
                                       " due to: " + e.getMessage());
        }
        return excludeList;
    }

    public boolean isExcluded(File file) {
        if (null != fileFilter) {
            do {
                if (fileFilter.accept(file)) {
                    log.info("{} matched one or more exclude rules: excluding...",
                             file.getAbsolutePath());
                    return true;
                }
                file = file.getParentFile();
            } while (file != null);
        }
        return false;
    }
}
