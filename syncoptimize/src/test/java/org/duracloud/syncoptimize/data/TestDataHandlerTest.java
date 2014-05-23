/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.data;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Bill Branan
 *         Date: 5/23/14
 */
public class TestDataHandlerTest {

     private File dir1 = new File("target/data-handler-test-dir-1");
     private File dir2 = new File("target/data-handler-test-dir-2");

    @After
    public void teardown() {
        if(dir1.exists()) {
            FileUtils.deleteQuietly(dir1);
        }
        if(dir2.exists()) {
            FileUtils.deleteQuietly(dir2);
        }
    }

    @Test
    public void testCreateRemoveDirectories() throws Exception {
        assertFalse(dir1.exists());
        assertFalse(dir2.exists());

        TestDataHandler dataHandler = new TestDataHandler();
        dataHandler.createDirectories(dir1, dir2);

        assertTrue(dir1.exists());
        assertTrue(dir2.exists());

        dataHandler.removeDirectories(dir1, dir2);

        assertFalse(dir1.exists());
        assertFalse(dir2.exists());
    }

    @Test
    public void testCreateTestData() throws Exception {
        dir1.mkdirs();
        int numFiles = 5;
        int xMB = 2;

        assertTrue(dir1.exists());
        assertEquals(0, dir1.listFiles().length);

        TestDataHandler dataHandler = new TestDataHandler();
        dataHandler.createTestData(dir1, numFiles, xMB);

        assertEquals(5, dir1.listFiles().length);
        for(File file : dir1.listFiles()) {
            assertEquals(2048, file.length());
        }
    }

}
