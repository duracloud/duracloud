/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import java.io.File;
import java.io.Serializable;

/**
 * @author: Bill Branan
 * Date: Apr 1, 2010
 */
public class ChangedFile implements Serializable {
    private File changedFile;
    private int syncAttempts;

    public ChangedFile(File changedFile) {
        this.changedFile = changedFile;
        syncAttempts = 0;
    }

    public File getFile() {
        return changedFile;
    }

    public int getSyncAttempts() {
        return syncAttempts;
    }

    public void incrementSyncAttempts() {
        syncAttempts++;
    }

    /**
     * Removes a previously reserved file from the ChangedList.
     */
    public void remove() {
        ChangedList.getInstance().remove(this);
    }

    /**
     * Releases the reservation on the file (if still reserved)
     * and returns it to the list of unreserved ChangedFiles.
     */
    public void unreserve() {
        ChangedList.getInstance().unreserve(this);
    }

}
