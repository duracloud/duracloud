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
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.event.EventListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The list of files which have been changed.
 *
 * @author: Bill Branan
 * Date: Mar 15, 2010
 */
public class ChangedList implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(ChangedList.class);
    private LinkedHashMap<String, ChangedFile> fileList;
    private LinkedHashMap<String, ChangedFile> reservedFiles;
    private ExecutorService executorService;
    private long listVersion;
    private boolean shutdown = false;

    private static ChangedList instance;

    private FileExclusionManager fileExclusionManager;
    private EventListenerSupport<ChangedListListener> listeners;

    public static synchronized ChangedList getInstance() {
        if (instance == null) {
            instance = new ChangedList();
        }
        return instance;
    }

    private ChangedList() {
        fileList = new LinkedHashMap<String, ChangedFile>();
        reservedFiles = new LinkedHashMap<String, ChangedFile>();
        this.fileExclusionManager = new FileExclusionManager();
        listVersion = 0;
        listeners =
            new EventListenerSupport<ChangedListListener>(ChangedListListener.class);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void setFileExclusionManager(FileExclusionManager fileExclusionManager) {
        if (fileExclusionManager == null) {
            throw new IllegalArgumentException("fileExclusionManager must not be null");
        }

        this.fileExclusionManager = fileExclusionManager;
    }

    /**
     * Adds a changed file to the list of items to be processed. If the file
     * happens to match exclusion rules it will not be added to the list (and
     * the method will return false). Note that only the most current update to
     * any given file is provided to the change processor.
     *
     * @param changedFile a file which has changed on the file system
     * @return false if the changedFile is null or matches at least one
     * exclusion rule.
     */
    public boolean addChangedFile(final File changedFile) {
        if (null != changedFile) {
            return addChangedFile(new ChangedFile(changedFile));
        } else {
            log.warn("The changedFile parameter was unexpectedly null. Ignored.");
            return false;
        }
    }

    /**
     * Gets the current size of the changed list
     *
     * @return the size of the list
     */
    public int getListSize() {
        return fileList.size();
    }

    /**
     * Gets the current size of the changed list included the files that have been reserved
     *
     * @return the size of the list
     */
    public int getListSizeIncludingReservedFiles() {
        return fileList.size() + reservedFiles.size();
    }

    synchronized boolean addChangedFile(ChangedFile changedFile) {
        File file = changedFile.getFile();
        if (fileExclusionManager.isExcluded(file)) {
            return false;
        }
        fileList.put(file.getAbsolutePath(), changedFile);
        incrementVersion();
        fireChangedEvent();
        return true;
    }

    protected void fireChangedEvent() {
        listeners.fire().listChanged(this);
    }

    protected void fireChangedEventAsync() {
        this.executorService.execute(new Runnable() {
            @Override
            public void run() {
                fireChangedEvent();
            }
        });
    }

    public void addListener(ChangedListListener listener) {
        this.listeners.addListener(listener);
    }

    public void removeListener(ChangedListListener listener) {
        this.listeners.removeListener(listener);
    }

    /**
     * Removes all files from the changed list.
     */
    public synchronized void clear() {
        fileList.clear();
        reservedFiles.clear();
        fireChangedEvent();
    }

    /**
     * Retrieves a changed file for processing and removes it from the list of unreserved files.
     * Returns null if there are no changed files in the list.
     *
     * @return a file which has changed on the file system
     */
    public synchronized ChangedFile reserve() {
        if (fileList.isEmpty() || shutdown) {
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
        if (listVersion < Long.MAX_VALUE) {
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
            Map<String, ChangedFile> fileListCopy;
            synchronized (this) {
                fileListCopy = (Map<String, ChangedFile>) fileList.clone();
                fileListCopy.putAll(reservedFiles);
                persistVersion = listVersion;
            }

            oStream.writeObject(fileListCopy);
            oStream.close();
            return persistVersion;
        } catch (IOException e) {
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
            log.info("Restoring changed list from backup: {}", persistFile.getAbsolutePath());
            synchronized (this) {
                LinkedHashMap<String, ChangedFile> fileListFromDisk =
                    (LinkedHashMap<String, ChangedFile>) oStream.readObject();

                //remove files in change list that are not in the content dir list.
                if (contentDirs != null && !contentDirs.isEmpty()) {

                    Iterator<Entry<String, ChangedFile>> entries =
                        fileListFromDisk.entrySet().iterator();
                    while (entries.hasNext()) {
                        Entry<String, ChangedFile> entry = entries.next();
                        ChangedFile file = entry.getValue();
                        boolean watched = false;
                        for (File contentDir : contentDirs) {
                            if (file.getFile()
                                    .getAbsolutePath()
                                    .startsWith(contentDir.getAbsolutePath()) &&
                                !this.fileExclusionManager.isExcluded(file.getFile())) {
                                watched = true;
                                break;
                            }
                        }

                        if (!watched) {
                            entries.remove();
                        }
                    }
                }

                this.fileList = fileListFromDisk;
            }
            oStream.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to restore File Changed List:" +
                                       e.getMessage(), e);
        }
    }

    public synchronized List<File> peek(int maxFiles) {
        List<File> files = new LinkedList<File>();
        Iterator<Entry<String, ChangedFile>> it = this.fileList.entrySet().iterator();
        int count = 0;
        while (it.hasNext() && count < maxFiles) {
            files.add(it.next().getValue().getFile());
            count++;
        }
        return files;
    }

    /**
     * Removes a previously reserved ChangedFile from the list of
     * reserved files, effectively removing it from the ChangedList.
     * However if this instance of the ChangedFile or a new
     * ChangedFile with an identical file path is re-added to the ChangedList
     * before the reserved file is removed,  calling remove will only remove
     * the changed file from the reserved list.
     *
     * @param changedFile
     */
    synchronized void remove(ChangedFile changedFile) {
        this.reservedFiles.remove(getKey(changedFile));
    }

    /**
     * Releases the reservation on the file (if still reserved) and returns
     * it to the list.
     *
     * @param changedFile
     */
    synchronized void unreserve(ChangedFile changedFile) {
        ChangedFile removedFile = this.reservedFiles.remove(getKey(changedFile));
        if (removedFile != null && !this.fileList.containsKey(getKey(removedFile))) {
            addChangedFile(removedFile);
        }
    }

    private String getKey(ChangedFile changedFile) {
        return changedFile.getFile().getAbsolutePath();
    }

    public void shutdown() {
        executorService.shutdown();
        shutdown = true;
        ChangedList.instance = null;
    }

}
