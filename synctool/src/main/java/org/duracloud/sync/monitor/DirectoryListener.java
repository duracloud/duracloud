/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.monitor;

import org.apache.commons.io.monitor.FilesystemListenerAdaptor;
import org.duracloud.sync.mgmt.ChangedList;

import java.io.File;

/**
 * Handles update notifications from the Directory Update Monitor by adding
 * the changed files to the ChangedList.
 *
 * @author: Bill Branan
 * Date: Mar 12, 2010
 */
public class DirectoryListener extends FilesystemListenerAdaptor {

    private ChangedList changedList;
    private boolean syncDeletes;

    public DirectoryListener(boolean syncDeletes) {
        changedList = ChangedList.getInstance();
        this.syncDeletes = syncDeletes;
    }

    @Override
    public void onFileCreate(File file) {
        addFileToChangedList(file);
    }

    @Override
    public void onFileChange(File file) {
        addFileToChangedList(file);
    }

    @Override
    public void onFileDelete(File file) {
        if(syncDeletes) {
            addFileToChangedList(file);
        }
    }

    private void addFileToChangedList(File file) {
        changedList.addChangedFile(file);
    }

}
