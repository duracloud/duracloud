/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.walker;

import org.duracloud.sync.mgmt.ChangedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: Mar 29, 2010
 */
public class DeleteChecker implements Runnable {

    private final Logger logger =
        LoggerFactory.getLogger(DeleteChecker.class);

    private Iterator<String> filesList;
    private List<File> syncDirs;

    /**
     * Creates a delete checker
     *
     * @param filesList list of relative file paths which exist in the endpoint
     * @param syncDirs the list of local source directories being synced
     */
    protected DeleteChecker(Iterator<String> filesList,
                            List<File> syncDirs) {
        this.filesList = filesList;
        this.syncDirs = syncDirs;
    }

    /**
     * Checks each item in the files list (relative file paths) against each
     * sync directory to see if there is a matching file. If there is no
     * matching file, that means that the file which exists in the endpoint
     * no longer exists in the local source directories (i.e. the source file
     * has been deleted.) Each file of this type is added to the ChangedList
     * to be handled by deletion in the endpoint.
     */
    public void run() {
        logger.info("Running Delete Checker");
        ChangedList changedList = ChangedList.getInstance();

        while (filesList.hasNext()) {
            String fileToCheck = filesList.next();
            logger.debug("Checking: " + fileToCheck);

            boolean exists = false;
            File checkFile = null;
            for (File syncDir : syncDirs) {
                checkFile = new File(syncDir, fileToCheck);
                if (checkFile.exists()) {
                    exists = true;
                }
            }

            if (!exists) {
                logger.debug("File: " + fileToCheck +
                    " does not exist locally, adding to list for delete.");
                if (checkFile != null) {
                    changedList.addChangedFile(checkFile);
                }
            }
        }
    }

    public static void start(Iterator<String> filesList,
                             List<File> syncDirs) {
        (new Thread(new DeleteChecker(filesList, syncDirs))).start();
    }
}
