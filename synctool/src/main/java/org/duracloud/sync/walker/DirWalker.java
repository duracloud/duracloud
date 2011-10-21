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

    private static DirWalker dirWalker;
    private boolean continueWalk;

    private List<File> topDirs;
    private ChangedList fileList;
    private int files = 0;
    private boolean complete = false;

    protected DirWalker(List<File> topDirs) {
        super();
        this.topDirs = topDirs;
        fileList = ChangedList.getInstance();
    }

    public void run() {
        walkDirs();
    }

    public void stopWalk() {
        continueWalk = false;
    }

    protected void walkDirs() {
        continueWalk = true;
        for(File dir : topDirs) {
            if(dir.exists() && dir.isDirectory() && continueWalk) {
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
        complete = true;
    }

    @Override
    protected void handleFile(File file, int depth, Collection results) {
        ++files;
        fileList.addChangedFile(file);
    }

    @Override
    protected boolean handleIsCancelled(File file,
                                        int depth,
                                        Collection results) throws IOException {
        return !continueWalk;
    }

    public static DirWalker start(List<File> topDirs) {
        dirWalker = new DirWalker(topDirs);
        (new Thread(dirWalker)).start();
        return dirWalker;
    }

    public boolean walkComplete() {
        return complete;
    }

    public int getFilesCount() {
        return files;
    }

}
