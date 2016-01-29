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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.event.EventListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The list of files which have been changed.
 *
 * @author: Bill Branan
 * Date: Mar 15, 2010
 */
public class ChangedList {

    private static final Logger log = LoggerFactory.getLogger(ChangedList.class);
    private LinkedHashMap<String, ChangedFile> fileList;
    private LinkedHashMap<String, ChangedFile> reservedFiles;
    
    private long listVersion;

    private static ChangedList instance;

    private EventListenerSupport<ChangedListListener> listeners;

    public static synchronized ChangedList getInstance() {
        if(instance == null) {
            instance = new ChangedList();
        }
        return instance;
    }

    private ChangedList() {
        fileList = new LinkedHashMap<String,ChangedFile>();
        reservedFiles = new LinkedHashMap<String,ChangedFile>();
        listVersion = 0;
        listeners =
            new EventListenerSupport<ChangedListListener>(ChangedListListener.class);
    }

    /**
     * Adds a changed file to the list of items to be processed.
     * Note that only the most current update to any given file is
     * provided to the change processor.
     *
     * @param changedFile a file which has changed on the file system
     */
    public void addChangedFile(final File changedFile) {
        if(null != changedFile){
            addChangedFile(new ChangedFile(changedFile));
        }else{
            log.warn("The changedFile parameter was unexpectedly null. Ignored.");
        }
    }

    /**
     * Gets the current size of the changed list
     * @return the size of the list
     */
    public int getListSize() {
        return fileList.size();
    }
    
    /**
     * Gets the current size of the changed list included the files that have been reserved
     * @return the size of the list
     */
    public int getListSizeIncludingReservedFiles() {
        return fileList.size() + reservedFiles.size();
    }

    protected synchronized void addChangedFile(ChangedFile changedFile) {
        fileList.put(changedFile.getFile().getAbsolutePath(), changedFile);
        incrementVersion();
        fireChangedEvent();
    }

    protected void fireChangedEvent() {
        listeners.fire().listChanged(this);
    }

    protected void fireChangedEventAsync() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                fireChangedEvent();
            }
        }).start();
    }

    public void addListener(ChangedListListener listener){
        this.listeners.addListener(listener);
    }

    public void removeListener(ChangedListListener listener){
        this.listeners.removeListener(listener);
    }

    /**
     * Removes all files from the changed list.
     */
    public synchronized void clear(){
        fileList.clear();
        reservedFiles.clear();
        fireChangedEvent();
    }

    /**
     * Retrieves a changed file for processing and removes it from the list of unreserved
     * files.  
     * Returns null if there are no changed files in the list.
     *
     * @return a file which has changed on the file system
     */
    public synchronized ChangedFile reserve() {
        if(fileList.isEmpty()) {
            return null;
        }

        String key = fileList.keySet().iterator().next();
        ChangedFile changedFile = fileList.remove(key);
        reservedFiles.put(key, changedFile);
        incrementVersion();
        fireChangedEventAsync();
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
    public  long persist(File persistFile) {
        try {
            FileOutputStream fileStream = new FileOutputStream(persistFile);
            ObjectOutputStream oStream = new ObjectOutputStream((fileStream));

            long persistVersion;
            Map<String, ChangedFile> fileListCopy;
            synchronized(this) {
                fileListCopy = (Map<String, ChangedFile>)fileList.clone();
                fileListCopy.putAll(reservedFiles);
                persistVersion = listVersion;
            }

            oStream.writeObject(fileListCopy);
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
     * @param contentDirs content directories currently configured.
     */
    public synchronized void restore(File persistFile, List<File> contentDirs) {
        try {
            FileInputStream fileStream = new FileInputStream(persistFile);
            ObjectInputStream oStream = new ObjectInputStream(fileStream);

            synchronized(this) {
                fileList = (LinkedHashMap<String, ChangedFile>) oStream.readObject();

                //remove files in change list that are not in the content dir list.
                if (contentDirs != null && !contentDirs.isEmpty()) {

                    Iterator<Entry<String, ChangedFile>> entries =
                        fileList.entrySet().iterator();
                    while (entries.hasNext()) {
                        Entry<String, ChangedFile> entry = entries.next();
                        ChangedFile file = entry.getValue();
                        boolean watched = false;
                        for (File contentDir : contentDirs) {
                            if (file.getFile()
                                    .getAbsolutePath()
                                    .startsWith(contentDir.getAbsolutePath())) {
                                watched = true;
                                break;
                            }
                        }

                        if (!watched) {
                            entries.remove();
                        }
                    }
                }
            }
            oStream.close();
        } catch(Exception e) {
            throw new RuntimeException("Unable to restore File Changed List:" +
                e.getMessage(), e);
        }
    }
    

    public synchronized List<File> peek(int maxFiles){
        List<File> files = new LinkedList<File>();
        Iterator<Entry<String, ChangedFile>> it = this.fileList.entrySet().iterator();
        int count = 0;
        while(it.hasNext() && count < maxFiles) {
            files.add(it.next().getValue().getFile());
            count++;
        }
        return files;
    }

    synchronized void  remove(ChangedFile changedFile) {
       this.reservedFiles.remove(getKey(changedFile));
    }
    
    synchronized void  unreserve(ChangedFile changedFile){
        ChangedFile removedFile = this.reservedFiles.remove(getKey(changedFile));
        if(removedFile != null){
            addChangedFile(removedFile);
        }
    }

    private String getKey(ChangedFile changedFile) {
        return changedFile.getFile().getAbsolutePath();
    }
        
}
