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

    public static void main(String[] args) throws Exception {
        // Parse command line options.
        SyncToolInitializer initializer = new SyncToolInitializer();
        SyncToolConfig syncConfig = initializer.processCommandLineArgs(args);

        // Start up the SyncTool
        SyncTool syncTool = new SyncTool();
        syncTool.setSyncConfig(syncConfig);
        syncTool.runSyncTool();
    }

    /**
     * Processes the command line arguments passed to the Sync Tool.
     *
     * @param args command line arguments
     * @return parsed configuration
     */
    private SyncToolConfig processCommandLineArgs(String[] args) {
        SyncToolConfigParser syncConfigParser = new SyncToolConfigParser();
        SyncToolConfig syncConfig = syncConfigParser.processCommandLine(args);
        return syncConfig;
    }

}
