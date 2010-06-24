/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * The list of files which have been changed.
 *
 * @author: Bill Branan
 * Date: Mar 15, 2010
 */
public class ChangedList {

    private HashMap<String, ChangedFile> fileList;
    private long listVersion;

    private static ChangedList instance;

    public static synchronized ChangedList getInstance() {
        if(instance == null) {
            instance = new ChangedList();
        }
        return instance;
    }

    private ChangedList() {
        fileList = new HashMap<String, ChangedFile>();
        listVersion = 0;
    }

    /**
     * Adds a changed file to the list of items to be processed.
     * Note that only the most current update to any given file is
     * provided to the change processor.
     *
     * @param changedFile a file which has changed on the file system
     */
    public void addChangedFile(File changedFile) {
        addChangedFile(new ChangedFile(changedFile));
    }

    /**
     * Gets the current size of the changed list
     * @return the size of the list
     */
    public int getListSize() {
        return fileList.size();
    }

    protected synchronized void addChangedFile(ChangedFile changedFile) {
        fileList.put(changedFile.getFile().getAbsolutePath(), changedFile);
        incrementVersion();
    }

    /**
     * Retrieves a changed file for processing and removes it from the list.
     * Returns null if there are no changed files in the list.
     *
     * @return a file which has changed on the file system
     */
    public synchronized ChangedFile getChangedFile() {
        if(fileList.isEmpty()) {
            return null;
        }

        String key = fileList.keySet().iterator().next();
        ChangedFile changedFile = fileList.remove(key);
        incrementVersion();
        return changedFile;
    }

    private void incrementVersion() {
        if(listVersion < Long.MAX_VALUE) {
            listVersion++;
        } else {
            listVersion = 0;
        }
    }

    public long getVersion() {
        return listVersion;
    }

    /**
     * Writes out the current state of the ChangeList to the given file.
     *
     * @param persistFile file to write state to
     * @return the version ID of the ChangedList which was persisted
     */
    public long persist(File persistFile) {
        try {
            FileOutputStream fileStream = new FileOutputStream(persistFile);
            ObjectOutputStream oStream = new ObjectOutputStream((fileStream));

            long persistVersion;
            HashMap<String, File> fileListClone;
            synchronized(this) {
                fileListClone = (HashMap<String, File>)fileList.clone();
                persistVersion = listVersion;
            }

            oStream.writeObject(fileListClone);
            oStream.close();
            return persistVersion;
        } catch(IOException e) {
            throw new RuntimeException("Unable to persist File Changed List:" +
                e.getMessage(), e);
        }
    }

    /**
     * Restores the state of the ChangedList using the given backup file
     *
     * @param persistFile file containing previous state
     */
    public void restore(File persistFile) {
        try {
            FileInputStream fileStream = new FileInputStream(persistFile);
            ObjectInputStream oStream = new ObjectInputStream(fileStream);

            synchronized(this) {
                fileList = (HashMap<String, ChangedFile>) oStream.readObject();
            }

            oStream.close();
        } catch(Exception e) {
            throw new RuntimeException("Unable to restore File Changed List:" +
                e.getMessage(), e);
        }
    }
    
}
