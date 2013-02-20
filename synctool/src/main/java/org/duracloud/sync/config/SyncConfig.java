/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.config;

import java.io.File;

/**
 * @author: Bill Branan
 * Date: 2/19/13
 */
public class SyncConfig {

    public static final String SYNC_WORK_PROP = "sync.work";

    public static final String DEFAULT_WORK_DIR =
        System.getProperty("user.home") + File.separator +
            "duracloud-sync-work";

    private static File workDir;

    public static void setWorkDir(File newWorkDir) {
        workDir = newWorkDir;
    }

    public static File getWorkDir() {
        if(null == workDir) {
            workDir = new File(System.getProperty(SYNC_WORK_PROP,
                                                  DEFAULT_WORK_DIR));
        }
        if (!workDir.exists()) {
            workDir.mkdirs();
            workDir.setWritable(true);
        }
        return workDir;
    }

}
