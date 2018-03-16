/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.duracloud.sync.config.SyncToolConfig;
import org.duracloud.sync.config.SyncToolConfigParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: 11/2/11
 */
public class SyncToolTest extends SyncTestBase {

    private SyncTool syncTool;
    private static final String prevHost = "prevHost";
    private static final String prevSpaceId = "prevSpaceId";
    private static final String prevStoreId = "prevStoreId";
    private static final boolean prevSyncDel = false;
    private static final boolean prevNoUpdates = false;
    private static final boolean prevRenameUpdates = false;
    private static final String prevPrefix = null;
    private static final File prevExclude = null;
    private static final String fileName1 = "/a/b/c.txt";
    private static final String fileName2 = "/a/b/d.txt";

    @Before
    @Override
    public void setUp() {
        syncTool = new SyncTool();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRestartPossible() throws Exception {
        // clean start = no restart
        SyncToolConfig config = new SyncToolConfig();
        config.setCleanStart(true);
        syncTool.setSyncConfig(config);

        assertFalse(syncTool.restartPossible());

        // no previous config = no restart
        File workDir = createTempDir("testwork");
        config.setCleanStart(false);
        config.setWorkDir(workDir);

        assertFalse(syncTool.restartPossible());

        // equal prev config = restart
        File contentDir = createTempDir("testcontent");
        String[] args = {"-h", prevHost,
                         "-s", prevSpaceId,
                         "-i", prevStoreId,
                         "-c", contentDir.getPath(),
                         "-u", "user",
                         "-p", "pass",
                         "-w", workDir.getAbsolutePath()};
        ConfigStorage storage = new ConfigStorage();
        storage.backupConfig(workDir, args);
        storage.backupConfig(workDir, args); // twice to push to prev config
        List<File> contentDirs = new ArrayList<>();
        contentDirs.add(contentDir);
        config = getConfig(prevHost, prevSpaceId, prevStoreId,
                           prevSyncDel, prevNoUpdates, prevRenameUpdates,
                           prevPrefix, prevExclude, contentDirs);
        config.setWorkDir(workDir);
        syncTool.setSyncConfig(config);

        assertTrue(syncTool.restartPossible());

        FileUtils.deleteQuietly(workDir);
        FileUtils.deleteQuietly(contentDir);
    }

    private class ConfigStorage extends SyncToolConfigParser {
        public void backupConfig(File backupDir, String[] args) {
            super.backupConfig(backupDir, args);
        }
    }

    @Test
    public void testConfigEquals() {
        SyncToolConfig prevConfig = getPrevConfig();
        assertTrue(syncTool.configEquals(prevConfig, prevConfig));

        SyncToolConfig currConfig = getPrevConfig();
        assertTrue(syncTool.configEquals(currConfig, prevConfig));

        currConfig = getPrevConfig();
        currConfig.setHost("host");
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        currConfig = getPrevConfig();
        currConfig.setSpaceId("spaceId");
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        currConfig = getPrevConfig();
        currConfig.setStoreId("storeId");
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        currConfig = getPrevConfig();
        currConfig.setStoreId(null);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        currConfig = getPrevConfig();
        currConfig.setSyncDeletes(true);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        currConfig = getPrevConfig();
        currConfig.setSyncUpdates(false);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        currConfig = getPrevConfig();
        currConfig.setRenameUpdates(true);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        currConfig = getPrevConfig();
        currConfig.setPrefix("prefix");
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        currConfig = getPrevConfig();
        currConfig.setExcludeList(new File(fileName1));
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        List<File> newDirs = new ArrayList<>();
        newDirs.add(new File(fileName1));
        currConfig = getPrevConfig();
        currConfig.setContentDirs(newDirs);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        newDirs = new ArrayList<>();
        newDirs.add(new File(fileName2));
        currConfig = getPrevConfig();
        currConfig.setContentDirs(newDirs);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));

        newDirs = new ArrayList<>();
        newDirs.add(new File("newFile.txt"));
        currConfig = getPrevConfig();
        currConfig.setContentDirs(newDirs);
        assertFalse(syncTool.configEquals(currConfig, prevConfig));
    }

    private SyncToolConfig getPrevConfig() {
        List<File> prevDirs = new ArrayList<>();
        prevDirs.add(new File(fileName1));
        prevDirs.add(new File(fileName2));

        return getConfig(prevHost, prevSpaceId, prevStoreId, prevSyncDel,
                         prevNoUpdates, prevRenameUpdates, prevPrefix,
                         prevExclude, prevDirs);
    }

    private SyncToolConfig getConfig(String host,
                                     String spaceId,
                                     String storeId,
                                     boolean syncDel,
                                     boolean noUpdates,
                                     boolean renameUpdates,
                                     String prefix,
                                     File exclude,
                                     List<File> contentDirs) {
        SyncToolConfig config = new SyncToolConfig();
        config.setHost(host);
        config.setSpaceId(spaceId);
        config.setStoreId(storeId);
        config.setSyncDeletes(syncDel);
        config.setSyncUpdates(!noUpdates);
        config.setRenameUpdates(renameUpdates);
        config.setPrefix(prefix);
        config.setExcludeList(exclude);
        config.setContentDirs(contentDirs);
        return config;
    }

}
