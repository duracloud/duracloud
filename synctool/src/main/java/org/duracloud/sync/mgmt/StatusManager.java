/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Singleton class which tracks the status of the sync queue
 *
 * @author: Bill Branan
 * Date: Apr 2, 2010
 */
public class StatusManager {

    private static final DateFormat DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private long inWork;
    private long succeeded;
    private List<SyncSummary> failed;
    private List<SyncSummary> recentlyCompleted;
    private String startTime;
    private ChangedList changedList;
    private String version;

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
        failed = new ArrayList<SyncSummary>();
        startTime = DATE_FORMAT.format(new Date());
        changedList = ChangedList.getInstance();
        recentlyCompleted = new LinkedList<SyncSummary>();
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

    public synchronized void successfulCompletion(SyncSummary summary) {
        succeeded++;
        inWork--;
        this.recentlyCompleted.add(0,summary);
        while (this.recentlyCompleted.size() > 100) {
            this.recentlyCompleted.remove(this.recentlyCompleted.size() - 1);
        }
    }

    public synchronized void failedCompletion(SyncSummary file) {
        failed.add(file);
        inWork--;
    }

    public long getInWork() {
        return inWork;
    }

    public long getSucceeded() {
        return succeeded;
    }

    public List<SyncSummary> getFailed() {
        return failed;
    }
    
    public List<SyncSummary> getRecentlyCompleted(){
        return new ArrayList<SyncSummary>(this.recentlyCompleted);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPrintableStatus() {
        StringBuilder status = new StringBuilder();

        status.append("\n---------------------------------------\n");
        status.append(" Sync Tool " + version + " - Status");
        status.append("\n--------------------------------------\n");
        status.append("Start Time: " + startTime + "\n");
        status.append("Current Time: " + DATE_FORMAT.format(new Date()) + "\n");
        status.append("Sync Queue Size: " + getQueueSize() + "\n");
        status.append("Syncs In Process: " + getInWork() + "\n");
        status.append("Successful Syncs: " + getSucceeded() + "\n");
        status.append("Failed Syncs: " + getFailed().size() + "\n");
        for(SyncSummary failedFile : getFailed()) {
            status.append("  " + failedFile.getFile().getAbsolutePath() + "\n");    
        }
        status.append("--------------------------------------\n");
        return status.toString();
    }

}
