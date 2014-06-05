/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.walker;

import org.duracloud.common.retry.Retriable;
import org.duracloud.common.retry.Retrier;
import org.duracloud.sync.endpoint.SyncEndpoint;
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

    private SyncEndpoint syncEndpoint;
    private String spaceId;
    private Iterator<String> filesList;
    private List<File> syncDirs;
    private boolean complete = false;
    private boolean stopped = false;
    private String prefix;
    
    /**
     * Creates a delete checker
     *
     * @param syncEndpoint the endpoint to which files are synced
     * @param syncDirs the list of local source directories being synced
     */
    protected DeleteChecker(SyncEndpoint syncEndpoint,
                            String spaceId,
                            List<File> syncDirs,
                            String prefix) {
        this.syncEndpoint = syncEndpoint;
        this.spaceId = spaceId;
        this.syncDirs = syncDirs;
        this.prefix = prefix;

        this.filesList = syncEndpoint.getFilesList();
    }

    /**
     * Checks each item in the list of files stored at the endpoint (relative
     * file paths) against each sync directory to see if there is a matching
     * local file. If there is no matching file, that means that the file which
     * exists in the endpoint no longer exists in the local source directories
     * (i.e. the source file has been deleted.) Each file of this type is
     * removed from the endpoint.
     *
     * Note that if a prefix is used, all files in the endpoint that do not
     * have the prefix will be removed (as they cannot be consistent with
     * what the content ID will be for files pushed up with the prefix.)
     */
    public void run() {
        logger.info("Running Delete Checker");

        while (filesList.hasNext() && !stopped) {
            String contentId = filesList.next();
            if(null != prefix) { // A prefix is being used
                if(contentId.startsWith(prefix)) {
                    if(!exists(contentId.substring(prefix.length()))) {
                        deleteContent(contentId);
                    }
                } else { // Content Id does not start with prefix
                    deleteContent(contentId);
                }
            } else { // A prefix is not being used
                if(!exists(contentId)) {
                    deleteContent(contentId);
                }
            }
        }
        complete = true;
    }

    private boolean exists(String fileToCheck) {
        boolean exists = false;
        for (File syncDir : syncDirs) {
            if (new File(syncDir, fileToCheck).exists()) {
                exists = true;
            }
        }
        logger.debug("Delete check on file: " + fileToCheck +
                     ". File exists: " + exists);
        return exists;
    }

    private void deleteContent(final String contentId) {
        try {
            new Retrier().execute(new Retriable() {
                @Override
                public String retry() throws Exception {
                    // The actual method being executed
                    syncEndpoint.deleteContent(spaceId, contentId);
                    return "success";
                }
            });
        } catch(Exception e) {
            logger.error("Failed to delete content item: " + contentId +
                         " from space: " + spaceId + " due to: " +
                         e.getMessage());
        }
    }

    public boolean checkComplete() {
        return complete;
    }
    
    public void stop() {
        this.stopped = true;
    }

    public static DeleteChecker start(SyncEndpoint syncEndpoint,
                                      String spaceId,
                                      List<File> syncDirs,
                                      String prefix) {
        DeleteChecker deleteChecker =
            new DeleteChecker(syncEndpoint, spaceId, syncDirs, prefix);
        (new Thread(deleteChecker)).start();
        return deleteChecker;
    }
}
