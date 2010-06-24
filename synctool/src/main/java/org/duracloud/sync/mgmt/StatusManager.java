/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Singleton class which tracks the status of the sync queue
 *
 * @author: Bill Branan
 * Date: Apr 2, 2010
 */
public class StatusManager {

    private long inWork;
    private long succeeded;
    private List<File> failed;
    private String startTime;
    private ChangedList changedList;

    private static StatusManager instance;

    public static StatusManager getInstance() {
        if(instance == null) {
            instance = new StatusManager();
        }
        return instance;
    }

    /*
     * Not to be used outside of tests
     */
    protected StatusManager() {
        succeeded = 0;
        failed = new ArrayList<File>();
        startTime =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        changedList = ChangedList.getInstance();
    }

    public int getQueueSize() {
        return changedList.getListSize();
    }

    public synchronized void startingWork() {
        inWork++;
    }

    public synchronized void stoppingWork() {
        inWork--;
    }

    public synchronized void successfulCompletion() {
        succeeded++;
        inWork--;
    }

    public synchronized void failedCompletion(File file) {
        failed.add(file);
        inWork--;
    }

    public long getInWork() {
        return inWork;
    }

    public long getSucceeded() {
        return succeeded;
    }

    public List<File> getFailed() {
        return failed;
    }

    public String getPrintableStatus() {
        StringBuilder status = new StringBuilder();

        status.append("\n--------------------------------------\n");
        status.append(" Sync Tool Status");
        status.append("\n--------------------------------------\n");
        status.append("Start Time: " + startTime + "\n");
        status.append("Sync Queue Size: " + getQueueSize() + "\n");
        status.append("Syncs In Process: " + getInWork() + "\n");
        status.append("Successful Syncs: " + getSucceeded() + "\n");
        status.append("Failed Syncs: " + getFailed().size() + "\n");
        for(File failedFile : getFailed()) {
            status.append("  " + failedFile.getAbsolutePath() + "\n");    
        }
        status.append("--------------------------------------\n");
        return status.toString();
    }

}
