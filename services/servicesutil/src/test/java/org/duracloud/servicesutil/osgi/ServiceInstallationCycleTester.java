/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.osgi;

import static junit.framework.Assert.assertNotNull;
import org.duracloud.services.common.util.BundleHome;
import org.duracloud.servicesutil.util.ServiceInstaller;
import org.duracloud.servicesutil.util.ServiceUninstaller;

/**
 * @author Andrew Woods
 *         Date: Dec 11, 2009
 */
public class ServiceInstallationCycleTester extends ServiceInstallTestBase {

    private ServiceInstaller installer;
    private ServiceUninstaller uninstaller;

    private final BundleHome bundleHome;

    public ServiceInstallationCycleTester(ServiceInstaller installer,
                                          ServiceUninstaller uninstaller) {
        assertNotNull(installer);
        assertNotNull(uninstaller);
        this.installer = installer;
        this.uninstaller = uninstaller;

        bundleHome = installer.getBundleHome();
        assertNotNull(bundleHome);
    }

    public void testServiceInstallationCycle() throws Exception {
        testJarInstallCycle();
        testZipInstallCycle();
    }

    private void testJarInstallCycle() throws Exception {
        verifyJarInstalled(bundleHome, false);

        installer.install(getBundleJarFilename(), getBundleJar());
        installer.install(getBundleJarFilename(), getBundleJar());
        installer.install(getBundleJarFilename(), getBundleJar());
        verifyJarInstalled(bundleHome, true);

        uninstaller.uninstall(getBundleJarFilename());
        verifyJarInstalled(bundleHome, true);

        uninstaller.uninstall(getBundleJarFilename());
        verifyJarInstalled(bundleHome, true);

        uninstaller.uninstall(getBundleJarFilename());
        verifyJarInstalled(bundleHome, false);

        deleteJarBundle(bundleHome);
    }

    private void testZipInstallCycle() throws Exception {
        verifyZipBagInstalled(bundleHome, false);

        String zipName0 = "0-" + ZIP_BAG_FILE_NAME;
        String zipName1 = "1-" + ZIP_BAG_FILE_NAME;
        String zipName2 = "2-" + ZIP_BAG_FILE_NAME;

        installer.install(zipName0, getZipBag());
        installer.install(zipName1, getZipBag());
        installer.install(zipName2, getZipBag());

        verifyZipBagInstalled(bundleHome, zipName0, true);

        uninstaller.uninstall(zipName0);
        verifyZipBagInstalled(bundleHome, zipName2, true);

        uninstaller.uninstall(zipName1);
        verifyZipBagInstalled(bundleHome, zipName2, true);

        uninstaller.uninstall(zipName2);
        verifyZipBagInstalled(bundleHome, zipName2, false);

        deleteZipBagBundles(bundleHome, zipName0);
        deleteZipBagBundles(bundleHome, zipName1);
        deleteZipBagBundles(bundleHome, zipName2);
    }

}
