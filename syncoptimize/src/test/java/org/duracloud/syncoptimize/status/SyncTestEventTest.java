/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.status;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Bill Branan
 *         Date: 5/23/14
 */
public class SyncTestEventTest {

    @Test
    public void testSyncTestEvent() {
        int threads = 30;
        long elapsed = 5000;
        int transferredMB = 20;

        SyncTestEvent event = new SyncTestEvent(threads, elapsed, transferredMB);
        assertEquals(threads, event.getThreads());
        assertEquals(elapsed, event.getElapsed());

        float seconds = elapsed / 1000f;
        float transferRate = (transferredMB * 8) / seconds;
        assertEquals(transferRate, event.getTransferRate(), 0);

        assertTrue(event.toString().contains(String.valueOf(threads)));
        assertTrue(event.toString().contains(String.valueOf(seconds)));
        assertTrue(event.toString().contains(String.valueOf(transferRate)));
    }

}
