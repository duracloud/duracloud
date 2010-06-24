/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil.tomcat;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Andrew Woods
 *         Date: Dec 1, 2009
 */
public class TomcatUtilTest extends TomcatTestBase {

    private TomcatUtil tomcatUtil;
    private File installDir;
    private final int port = 11080;

    @Before
    public void setUp() {
        String tomcatZipName = "fake-tomcat.zip";
        installDir = getInstallDir("install");

        tomcatUtil = new TomcatUtil();
        tomcatUtil.setResourceDir(getResourceDir());
        tomcatUtil.setBinariesZipName(tomcatZipName);
    }

    @Test
    public void testInstall() throws IOException {
        doInstall();
    }

    private TomcatInstance doInstall() throws IOException {
        TomcatInstance instance = tomcatUtil.installTomcat(installDir, port);
        int numFiles = 7;
        verifyInstall(instance, numFiles);

        return instance;
    }

    @Test
    public void testUnInstall() throws IOException {
        TomcatInstance instance = doInstall();
        tomcatUtil.unInstallTomcat(instance);

        int numFiles = 0;
        verifyInstall(instance, numFiles);
    }

}
