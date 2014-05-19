/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.status;

import java.util.ArrayList;
import java.util.List;

/**
 * Captures the status of sync optimization tests. Each test run is captured
 * as a SyncTestEvent.
 *
 * @author Bill Branan
 *         Date: 5/16/14
 */
public class SyncTestStatus {

    private static final Float MILLIS_IN_A_SEC = 1000f;

    private boolean printEvents;
    private List<SyncTestEvent> syncEvents;
    private int transferedMB = 1;

    public SyncTestStatus(boolean printEvents) {
        this.syncEvents = new ArrayList<>();
        this.printEvents = printEvents;
    }

    public void setTransferedMB(int transferedMB) {
        this.transferedMB = transferedMB;
    }

    public void addEvent(SyncTestEvent event) {
        syncEvents.add(event);

        if(printEvents) {
            float seconds = event.getElapsed() / MILLIS_IN_A_SEC;
            float rate = (transferedMB * 8) / seconds; // Mb per sec
            System.out.println("### Test with " + event.getThreads() +
                               " threads required " + seconds + " seconds. " +
                               "Transfer rate: " + rate + " Mbps.");
        }
    }

    public List<SyncTestEvent> getSyncEvents() {
        return syncEvents;
    }

}
