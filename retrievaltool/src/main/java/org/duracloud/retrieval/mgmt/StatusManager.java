/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Singleton class which tracks the status of the retrieval activity
 *
 * @author: Bill Branan
 * Date: Oct 12, 2010
 */
public class StatusManager {

    private static final DateFormat DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private long inWork;
    private long noChange;
    private long succeeded;
    private long failed;
    private long missing;
    private String startTime;
    private String version;

    private static StatusManager instance;

    public static StatusManager getInstance() {
        if (instance == null) {
            instance = new StatusManager();
        }
        return instance;
    }

    private StatusManager() {
        reset();
    }

    /*
     * Not to be used outside of tests
     */
    protected void reset() {
        inWork = 0;
        noChange = 0;
        succeeded = 0;
        failed = 0;
        missing = 0;
        startTime = DATE_FORMAT.format(new Date());
    }

    public synchronized void startingWork() {
        inWork++;
    }

    public synchronized void noChangeCompletion() {
        noChange++;
        inWork--;
    }

    public synchronized void successfulCompletion() {
        succeeded++;
        inWork--;
    }

    public synchronized void failedCompletion() {
        failed++;
        inWork--;
    }

    public synchronized void missingCompletion() {
        missing++;
        inWork--;
    }

    public long getInWork() {
        return inWork;
    }

    public long getNoChange() {
        return noChange;
    }

    public long getSucceeded() {
        return succeeded;
    }

    public long getFailed() {
        return failed;
    }

    public long getMissing() {
        return missing;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPrintableStatus() {
        StringBuilder status = new StringBuilder();

        status.append("\n--------------------------------------\n");
        status.append(" Retrieval Tool " + version + " - Status");
        status.append("\n--------------------------------------\n");
        status.append("Start Time: " + startTime + "\n");
        status.append("Current Time: " + DATE_FORMAT.format(new Date()) + "\n");
        status.append("Retrievals In Process: " + getInWork() + "\n");
        status.append("Successful Retrievals: " + getSucceeded() + "\n");
        status.append("No Change Needed: " + getNoChange() + "\n");
        status.append("Failed Retrievals: " + getFailed() + "\n");
        status.append("Missing files: " + getMissing() + "\n");
        status.append("--------------------------------------\n");
        return status.toString();
    }

}
