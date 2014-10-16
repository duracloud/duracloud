/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.config;

import java.io.File;

/**
 * @author Bill Branan
 *         Date: 10/16/14
 */
public class RetrievalConfig {

    public static final String RETRIEVAL_WORK_PROP = "retrieval.work";

    public static final String DEFAULT_WORK_DIR =
        System.getProperty("user.home") + File.separator +
            "duracloud-retreival-work";

    private static File workDir;

    public static void setWorkDir(File newWorkDir) {
        workDir = newWorkDir;
    }

    public static File getWorkDir() {
        // Determine the work dir. If a value was provided by the user, it
        // will be used, otherwise a default work dir in the user home is used.
        if(null == workDir) {
            workDir = new File(System.getProperty(RETRIEVAL_WORK_PROP,
                                                  DEFAULT_WORK_DIR));
        }
        if (!workDir.exists()) {
            workDir.mkdirs();
            workDir.setWritable(true);
        }

        // Sets the retrieval.work system property, so that the logs will be created
        // in the correct location. The logback.xml file expects retrieval.work to
        // be defined.
        // Note: It is very important that no instance of Logger is used prior
        // to this property being set. Doing so will cause the logs to be
        // written to a retrival.work_IS_UNDEFINED directory rather than the
        // preferred work directory.
        System.setProperty(RETRIEVAL_WORK_PROP, workDir.getAbsolutePath());

        return workDir;
    }

}
