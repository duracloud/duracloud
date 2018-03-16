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

    protected SyncConfig() {
        // Ensures no instances are made of this class, as there are only static members.
    }

    public static void setWorkDir(File newWorkDir) {
        workDir = newWorkDir;
    }

    public static File getWorkDir() {
        // Determine the work dir. If a value was provided by the user, it
        // will be used, otherwise a default work dir in the user home is used.
        if (null == workDir) {
            workDir = new File(System.getProperty(SYNC_WORK_PROP,
                                                  DEFAULT_WORK_DIR));
        }
        if (!workDir.exists()) {
            workDir.mkdirs();
            workDir.setWritable(true);
        }

        // Sets the sync.work system property, so that the logs will be created
        // in the correct location. The logback.xml file expects sync.work to
        // be defined.
        // Note: It is very important that no instance of Logger is used prior
        // to this property being set. Doing so will cause the logs to be
        // written to a sync.work_IS_UNDEFINED directory rather than the
        // preferred work directory.
        System.setProperty(SYNC_WORK_PROP, workDir.getAbsolutePath());

        return workDir;
    }

}
