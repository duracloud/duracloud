/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.osgi;

import static junit.framework.Assert.assertNotNull;
import org.duracloud.services.common.error.ServiceException;
import org.duracloud.services.common.util.BundleHome;
import org.duracloud.servicesutil.util.ServiceInstaller;

import java.io.IOException;

/**
 * @author Andrew Woods
 */
public class ServiceInstallerTester extends ServiceInstallTestBase {

    private final ServiceInstaller installer;
    private final BundleHome bundleHome;

    public ServiceInstallerTester(ServiceInstaller installer) {
        assertNotNull(installer);
        this.installer = installer;

        bundleHome = installer.getBundleHome();
        assertNotNull(bundleHome);
    }

    public void testServiceInstaller() throws Exception {
        testDummyInstall();
        testJarInstall();
        testZipInstall();
    }

    private void testDummyInstall() throws Exception {
        verifyDummyBundleInstalled(bundleHome.getContainer(), false);

        installer.install(DUMMY_BUNDLE_FILE_NAME, getDummyBundle());
        verifyDummyBundleInstalled(bundleHome.getContainer(), true);

        deleteDummyBundle(bundleHome);
    }

    private void testJarInstall() throws IOException, ServiceException {
        verifyJarInstalled(bundleHome, false);

        installer.install(getBundleJarFilename(), getBundleJar());
        verifyJarInstalled(bundleHome, true);

        deleteJarBundle(bundleHome);
    }

    private void testZipInstall() throws IOException, ServiceException {
        verifyZipBagInstalled(bundleHome, false);

        installer.install(ZIP_BAG_FILE_NAME, getZipBag());
        verifyZipBagInstalled(bundleHome, true);

        deleteZipBagBundles(bundleHome);
    }

}
