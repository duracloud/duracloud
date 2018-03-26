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
 * Date: 5/16/14
 */
public class SyncTestStatus {

    private boolean printEvents;
    private List<SyncTestEvent> syncEvents;

    public SyncTestStatus(boolean printEvents) {
        this.syncEvents = new ArrayList<>();
        this.printEvents = printEvents;
    }

    public void addEvent(SyncTestEvent event) {
        syncEvents.add(event);

        if (printEvents) {
            System.out.println("### " + event.toString());
        }
    }

    public List<SyncTestEvent> getSyncEvents() {
        return syncEvents;
    }

}
