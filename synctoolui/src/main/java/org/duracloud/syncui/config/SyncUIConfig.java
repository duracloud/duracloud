/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.config;

import org.duracloud.sync.config.SyncConfig;

import java.io.File;

/**
 * @author: Bill Branan
 * Date: 2/18/13
 */
public class SyncUIConfig extends SyncConfig {

    public static final String SYNC_CONFIG_PROP = "sync.config";
    public static final String SYNC_CONTEXT_PROP = "sync.context";
    public static final String SYNC_PORT_PROP = "sync.port";

    private static final String DEFAULT_CONFIG_XML_PATH =
        getWorkDir().getAbsolutePath() + File.separator +
            "duracloud-sync-ui-config.xml";

    public static String getConfigPath() {
        return System.getProperty(SYNC_CONFIG_PROP, DEFAULT_CONFIG_XML_PATH);
    }

    public static String getContextPath() {
        return System.getProperty(SYNC_CONTEXT_PROP, "/sync");
    }

    public static int getPort() {
        return Integer.parseInt(System.getProperty(SYNC_PORT_PROP, "8888"));
    }

}
