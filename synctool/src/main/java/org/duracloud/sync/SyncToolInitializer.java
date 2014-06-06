/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync;

import org.duracloud.sync.config.SyncToolConfig;
import org.duracloud.sync.config.SyncToolConfigParser;

/**
 * Initializes the configuration of the Sync Tool.
 *
 * @author: Bill Branan
 * Date: 2/19/13
 */
public class SyncToolInitializer {

    public static void main(String[] args) {
        SyncToolInitializer initializer = new SyncToolInitializer();
        initializer.runSyncTool(args);
    }

    public void runSyncTool(String[] args) {
        // Parse command line options.
        SyncToolConfigParser syncConfigParser = new SyncToolConfigParser();
        SyncToolConfig syncConfig = syncConfigParser.processCommandLine(args);

        // Start up the SyncTool
        SyncTool syncTool = new SyncTool();
        syncTool.setSyncConfig(syncConfig);
        syncTool.runSyncTool();
    }

}
