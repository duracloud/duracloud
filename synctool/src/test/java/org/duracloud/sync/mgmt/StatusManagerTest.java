/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import static junit.framework.Assert.assertEquals;
import org.junit.Test;

import java.io.File;

/**
 * @author: Bill Branan
 * Date: Apr 2, 2010
 */
public class StatusManagerTest {

    @Test
    public void testStatusManager() {
        StatusManager status = new StatusManager();
        assertEquals(0, status.getInWork());
        assertEquals(0, status.getSucceeded());
        assertEquals(0, status.getFailed().size());

        for(int i=0; i<100; i++) {
            status.startingWork();
        }

        assertEquals(100, status.getInWork());
        assertEquals(0, status.getSucceeded());
        assertEquals(0, status.getFailed().size());

        for(int i=0; i<50; i++) {
            status.successfulCompletion();
        }

        assertEquals(50, status.getInWork());
        assertEquals(50, status.getSucceeded());
        assertEquals(0, status.getFailed().size());

        for(int i=0; i<50; i++) {
            status.failedCompletion(new File("test"));
        }

        assertEquals(0, status.getInWork());
        assertEquals(50, status.getSucceeded());
        assertEquals(50, status.getFailed().size());
    }
}
