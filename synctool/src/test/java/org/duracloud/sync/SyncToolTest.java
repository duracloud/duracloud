/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync;

import org.duracloud.sync.config.SyncToolConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: 11/2/11
 */
public class SyncToolTest {

    private SyncTool syncTool;

    @Before
    public void setup() {
        syncTool = new SyncTool();
    }

    @Test
    public void testConfigEquals() {
        String prevHost = "prevHost";
        String prevSpaceId = "prevSpaceId";
        String prevStoreId = "prevStoreId";
        boolean prevSyncDel = false;
        List<File> prevDirs = new ArrayList<File>();
        String fileName1 = "/a/b/c.txt";
        String fileName2 = "/a/b/d.txt";
        prevDirs.add(new File(fileName1));
        prevDirs.add(new File(fileName2));

        SyncToolConfig prevConfig =
            getConfig(prevHost, prevSpaceId, prevStoreId, prevSyncDel, prevDirs);
        assertTrue(syncTool.configEquals(prevConfig, prevConfig));

        SyncToolConfig currConfig =
            getConfig(prevHost, prevSpaceId, prevStoreId, prevSyncDel, prevDirs);
        assertTrue(syncTool.configEquals(currConfig, prevConfig));

        currConfig =
            getConfig("host", prevSpaceId, prevStoreId, prevSyncDel, prevDirs);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        currConfig =
            getConfig(prevHost, "spaceId", prevStoreId, prevSyncDel, prevDirs);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        currConfig =
            getConfig(prevHost, prevSpaceId, "storeId", prevSyncDel, prevDirs);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        currConfig =
            getConfig(prevHost, prevSpaceId, null, prevSyncDel, prevDirs);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        currConfig =
            getConfig(prevHost, prevSpaceId, prevStoreId, true, prevDirs);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        List<File> newDirs = new ArrayList<File>();
        newDirs.add(new File(fileName1));
        currConfig =
            getConfig(prevHost, prevSpaceId, prevStoreId, prevSyncDel, newDirs);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        newDirs = new ArrayList<File>();
        newDirs.add(new File(fileName2));
        currConfig =
            getConfig(prevHost, prevSpaceId, prevStoreId, prevSyncDel, newDirs);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        newDirs = new ArrayList<File>();
        newDirs.add(new File("newFile.txt"));
        currConfig =
            getConfig(prevHost, prevSpaceId, prevStoreId, prevSyncDel, newDirs);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));
    }

    private SyncToolConfig getConfig(String host,
                                     String spaceId,
                                     String storeId,
                                     boolean syncDel,
                                     List<File> contentDirs) {
        SyncToolConfig config = new SyncToolConfig();
        config.setHost(host);
        config.setSpaceId(spaceId);
        config.setStoreId(storeId);
        config.setSyncDeletes(syncDel);
        config.setContentDirs(contentDirs);
        return config;
    }

}
