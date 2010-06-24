/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.walker;

import org.apache.commons.io.DirectoryWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.duracloud.sync.mgmt.ChangedList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handles the walking of a set of directory trees. Each file found in the
 * tree is added to the changed file list. This is the starting point
 * for synchronization.
 *
 * @author: Bill Branan
 * Date: Mar 17, 2010
 */
public class DirWalker extends DirectoryWalker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(DirWalker.class);

    private List<File> topDirs;
    private ChangedList fileList;
    private int files = 0;

    protected DirWalker(List<File> topDirs) {
        super();
        this.topDirs = topDirs;
        fileList = ChangedList.getInstance();
    }

    public void run() {
        walkDirs();
    }

    protected void walkDirs() {
        for(File dir : topDirs) {
            if(dir.exists() && dir.isDirectory()) {
                try {
                    List results = new ArrayList();
                    walk(dir, results);
                } catch(IOException e) {
                    throw new RuntimeException("Error walking directory " +
                        dir.getAbsolutePath() + ":" + e.getMessage(), e);
                }
            } else {
                logger.warn("Skipping " + dir.getAbsolutePath() +
                            ", as it does not point to a directory");
            }
        }
        logger.info("Found " + files +
            " files to sync in initial directory walk");
    }

    @Override
    protected void handleFile(File file, int depth, Collection results) {
        ++files;
        fileList.addChangedFile(file);
    }

    public static void start(List<File> topDirs) {
        (new Thread(new DirWalker(topDirs))).start();       
    }

}
