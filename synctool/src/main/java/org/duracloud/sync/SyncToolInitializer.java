/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync;

import org.duracloud.sync.config.SyncConfig;
import org.duracloud.sync.config.SyncToolConfig;
import org.duracloud.sync.config.SyncToolConfigParser;

import java.io.File;

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

        // Determine the work dir. If a value is provided by the user, it
        // will be used, otherwise a default work dir in the user home is used.
        if(null != syncConfig.getWorkDir()) {
            SyncConfig.setWorkDir(syncConfig.getWorkDir());
        }
        File workDir = SyncConfig.getWorkDir();

        // Sets the sync.work system property, so that the logs will be created
        // in the correct location. The logback.xml file expects sync.work to
        // be defined.
        // Note: It is very important that no instance of Logger is used prior
        // to this point in the application. Doing so will cause the logs to
        // be written to a sync.work_IS_UNDEFINED directory rather than the
        // preferred work directory.
        System.setProperty(SyncConfig.SYNC_WORK_PROP,
                           workDir.getAbsolutePath());
        syncConfig.setWorkDir(workDir);

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
