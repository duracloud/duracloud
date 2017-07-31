/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.walker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.duracloud.sync.mgmt.ChangedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the walking of a set of directory trees. Each file found in the
 * tree is added to the changed file list. Any files found among the listed
 * directories will also be added to the changed file list. This is the
 * starting point for synchronization.
 *
 * @author: Bill Branan
 * Date: Mar 17, 2010
 */
public class DirWalker extends DirectoryWalker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(DirWalker.class);

    private static DirWalker dirWalker;
    private boolean continueWalk;

    private List<File> filesAndDirs;
 
    protected final ChangedList changedList;
    private int files = 0;
    private boolean complete = false;

    protected DirWalker(List<File> filesAndDirs) {
        super();
        this.filesAndDirs = filesAndDirs;
        this.changedList = ChangedList.getInstance();
    }


    public void run() {
        walkDirs();
    }

    public void stopWalk() {
        continueWalk = false;
    }

    protected void walkDirs() {
        try{
            continueWalk = true;
            for(File item : filesAndDirs) {
                if(null != item && item.exists() && continueWalk) {
                    if(item.isDirectory()) { // Directory
                        try {
                            List results = new ArrayList();
                            walk(item, results);
                        } catch(IOException e) {
                            throw new RuntimeException("Error walking directory " +
                                item.getAbsolutePath() + ":" + e.getMessage(), e);
                        }
                    } else { // File
                        handleFile(item, 0, null);
                    }
                } else {
                    String filename = "null";
                    if(item !=null){
                        filename = item.getAbsolutePath();
                    }
                    logger.warn("Skipping " + filename +
                                ", as it does not exist");
                }
            }
            logger.info("Found " + files +
                " files to sync in initial directory walk");
        
        }catch(Exception e){
            logger.error("dir walker failed: " + e.getMessage(), e);
        }

        complete = true;
    }

    @Override
    protected void handleFile(File file, int depth, Collection results) {
        if( null == file){
            logger.warn("The file parameter is unexpectedly null. Ignoring...");
        } else {
            if(changedList.addChangedFile(file)){
                ++files;
            }
        } 
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
