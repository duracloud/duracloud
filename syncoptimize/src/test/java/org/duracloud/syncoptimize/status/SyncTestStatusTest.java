/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * @author Bill Branan
 * Date: 5/23/14
 */
public class SyncTestStatusTest {

    @Test
    public void testSyncTestStatus() {
        SyncTestEvent event1 = new SyncTestEvent(1, 1, 1);
        SyncTestEvent event2 = new SyncTestEvent(2, 2, 2);

        SyncTestStatus syncTestStatus = new SyncTestStatus(false);
        syncTestStatus.addEvent(event1);
        syncTestStatus.addEvent(event2);

        List<SyncTestEvent> events = syncTestStatus.getSyncEvents();
        assertNotNull(events);
        assertEquals(2, events.size());
        assertTrue(events.contains(event1));
        assertTrue(events.contains(event2));
    }

}
