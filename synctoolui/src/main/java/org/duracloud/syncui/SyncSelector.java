/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui;

import org.duracloud.sync.SyncToolInitializer;
import org.duracloud.syncui.config.SyncUIConfig;

/**
 * Runs the sync application in either the command line mode or GUI mode. If
 * command line parameters are present, command line mode is selected, otherwise
 * GUI mode is run.
 *
 * @author: Bill Branan
 * Date: 2/18/13
 */
public class SyncSelector {

    public static void main(String[] args) throws Exception {
        // Set up default work dir path
        String workDirPath = System.getProperty(SyncUIConfig.SYNC_WORK_PROP);
        if(null == workDirPath) {
            System.setProperty(SyncUIConfig.SYNC_WORK_PROP,
                               SyncUIConfig.DEFAULT_WORK_DIR);
        }

        // Determine which tool to execute
        if(args.length > 0) {
            SyncToolInitializer.main(args);
        } else {
            SyncUIDriver.main(args);
        }
    }

}
