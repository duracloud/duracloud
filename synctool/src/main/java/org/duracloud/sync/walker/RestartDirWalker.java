/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.walker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Walks a set of directory trees just like a DirWalker, but only adds files
 * to the changed list if their modified date is more recent than the time of
 * the last backup. This provides a listing of files which have been added or
 * updated since the last backup.
 *
 * All files in directories which have changed are added to the changed list as
 * well in order to handle the possibility of directory names having been
 * changed.
 *
 * @author: Bill Branan
 * Date: Mar 24, 2010
 */
public class RestartDirWalker extends DirWalker {

    private long lastBackup;
    private List<File> changedDirs;

    protected RestartDirWalker(List<File> topDirs, long lastBackup) {
        super(topDirs);
        this.lastBackup = lastBackup;
        changedDirs = new ArrayList<File>();
    }

    @Override
    protected void walkDirs() {
        super.walkDirs();

        // Walk and add all files in directories which have changed
        if(changedDirs.size() > 0) {
            DirWalker dirWalker = new DirWalker(changedDirs);
            dirWalker.walkDirs();
        }
    }

    @Override
    protected void handleFile(File file, int depth, Collection results) {
        if(file.lastModified() > lastBackup) {
            super.handleFile(file, depth, results);
        }
    }

    @Override
    protected boolean handleDirectory(File directory,
                                      int depth,
                                      Collection results) {
        if(directory.lastModified() > lastBackup) {
            changedDirs.add(directory);
        }
        return true;
    }

    public static void start(List<File> topDirs, long lastBackup) {
        (new Thread(new RestartDirWalker(topDirs, lastBackup))).start();       
    }
}