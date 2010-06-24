/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.internal;

import org.apache.commons.io.FileUtils;
import org.duracloud.services.common.util.BundleHome;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;

/**
 * @author Andrew Woods
 *         Date: Dec 13, 2009
 */
public class ServiceUninstallerImplTest extends ServiceInstallImplTestBase {

    private ServiceInstallerImpl installer;
    private ServiceUninstallerImpl uninstaller;

    private InputStream bagZip;

    @Before
    public void setUp() throws Exception {
        installer = new ServiceInstallerImpl();
        installer.setBundleHome(new BundleHome());

        uninstaller = new ServiceUninstallerImpl();
        uninstaller.setBundleHome(new BundleHome());

        bagZip = createBagZip();
    }

    private InputStream createBagZip() throws Exception {
        return createBagArchive(File.createTempFile(name, ".zip"));
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteQuietly(installer.getBundleHome().getHome());

        if (bagZip != null) {
            bagZip.close();
        }
    }

    @Test
    public void testZipInstallCycle() throws Exception {

        // Install
        installer.install(nameZip, bagZip);

        File container = installer.getBundleHome().getContainer();
        File attic = installer.getBundleHome().getAttic();
        File workDir = installer.getBundleHome().getWork();

        File atticBag = new File(attic, nameZip);
        verifyExists(true, atticBag);

        File bundle = new File(container, entryName0);
        verifyExists(true, bundle);

        File noBundle = new File(container, entryName1);
        verifyExists(false, noBundle);

        File serviceWorkDir = new File(workDir, name); // no extension
        verifyExists(true, serviceWorkDir);
        File war = new File(serviceWorkDir, entryName2);
        verifyExists(true, war);

        // Uninstall
        uninstaller.uninstall(nameZip);

        verifyExists(false, atticBag);
        verifyExists(false, bundle);
        verifyExists(false, noBundle);
        verifyExists(false, serviceWorkDir);
        verifyExists(false, war);
    }

}
