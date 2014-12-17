/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.test;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.sync.SyncToolInitializer;
import org.duracloud.sync.mgmt.ChangedList;
import org.duracloud.sync.mgmt.StatusManager;
import org.duracloud.syncoptimize.config.SyncOptimizeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handles running a single sync action using the SyncTool and capturing the
 * time required for the test to complete.
 *
 * @author Bill Branan
 *         Date: 5/16/14
 */
public class SyncTester {

    private SyncOptimizeConfig syncOptConfig;
    private File dataDir;
    private File workDir;
    private ContentStore contentStore;
    private String prefix;

    private final Logger log = LoggerFactory.getLogger(SyncTester.class);

    public SyncTester(SyncOptimizeConfig syncOptConfig,
                      File dataDir,
                      File workDir,
                      ContentStore contentStore,
                      String prefix) {
        this.syncOptConfig = syncOptConfig;
        this.dataDir = dataDir;
        this.workDir = workDir;
        this.contentStore = contentStore;
        this.prefix = prefix;
    }

    public long runSyncTest(int threads) {
        cleanupSync();
        long start = System.currentTimeMillis();
        performSync(threads);
        long end = System.currentTimeMillis();
        cleanupSync();
        return end - start;
    }

    private void performSync(int threads) {
        SyncToolInitializer syncTool = getSyncTool();

        List<String> args = new ArrayList<>();
        args.add("-h");
        args.add(syncOptConfig.getHost());
        args.add("-s");
        args.add(syncOptConfig.getSpaceId());
        args.add("-u");
        args.add(syncOptConfig.getUsername());
        args.add("-p");
        args.add(syncOptConfig.getPassword());
        args.add("-c");
        args.add(dataDir.getAbsolutePath());
        args.add("-w");
        args.add(workDir.getAbsolutePath());
        args.add("-a");
        args.add(prefix);
        args.add("-x");
        args.add("-l");
        args.add("-j");
        args.add("-t");
        args.add(String.valueOf(threads));

        syncTool.runSyncTool(args.toArray(new String[]{}));
    }

    protected SyncToolInitializer getSyncTool() {
        return new SyncToolInitializer();
    }

    protected void cleanupSync() {
        try {
            String spaceId = syncOptConfig.getSpaceId();
            Iterator<String> testContent =
                contentStore.getSpaceContents(spaceId, prefix);
            while(testContent.hasNext()) {
                contentStore.deleteContent(spaceId, testContent.next());
            }
        } catch(ContentStoreException e) {
            log.error("Error cleaning up DuraStore content: " +
                      e.getMessage());
        }

        ChangedList.getInstance().clear();
        StatusManager.getInstance().clear();
    }

}
