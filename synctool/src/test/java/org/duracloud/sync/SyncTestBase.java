/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync;

import org.duracloud.sync.mgmt.ChangedList;
import org.junit.Before;
import org.junit.After;
import static junit.framework.Assert.assertNull;

import java.io.File;

/**
 * @author: Bill Branan
 * Date: Mar 25, 2010
 */
public class SyncTestBase {
    
    protected ChangedList changedList;

    @Before
    public void setUp() throws Exception {
        changedList = ChangedList.getInstance();
        changedList.clear();
    }

    @After
    public void tearDown() throws Exception {
        changedList.clear();
    }

    protected File createTempDir(String dirName) {
        File tempDir = new File("target", dirName);
        if(!tempDir.exists()) {
            tempDir.mkdir();
        }
        return tempDir;
    }
}
