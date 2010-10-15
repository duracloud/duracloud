/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author: Bill Branan
 * Date: Oct 14, 2010
 */
public class StatusManagerTest {

    @Test
    public void testStatusManager() {
        StatusManager status = StatusManager.getInstance();
        status.reset();

        assertEquals(0, status.getInWork());
        assertEquals(0, status.getSucceeded());
        assertEquals(0, status.getNoChange());
        assertEquals(0, status.getFailed());

        for(int i=0; i<150; i++) {
            status.startingWork();
        }

        assertEquals(150, status.getInWork());
        assertEquals(0, status.getSucceeded());
        assertEquals(0, status.getNoChange());
        assertEquals(0, status.getFailed());

        for(int i=0; i<50; i++) {
            status.successfulCompletion();
        }

        assertEquals(100, status.getInWork());
        assertEquals(50, status.getSucceeded());
        assertEquals(0, status.getNoChange());
        assertEquals(0, status.getFailed());

        for(int i=0; i<50; i++) {
            status.noChangeCompletion();
        }

        assertEquals(50, status.getInWork());
        assertEquals(50, status.getSucceeded());
        assertEquals(50, status.getNoChange());
        assertEquals(0, status.getFailed());

        for(int i=0; i<50; i++) {
            status.failedCompletion();
        }

        assertEquals(0, status.getInWork());
        assertEquals(50, status.getSucceeded());
        assertEquals(50, status.getNoChange());
        assertEquals(50, status.getFailed());
    }

}
