/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.osgi;

import static junit.framework.Assert.assertNotNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.services.common.util.BundleHome;
import org.duracloud.servicesutil.util.ServiceUninstaller;
import org.duracloud.servicesutil.util.catalog.BundleCatalog;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Andrew Woods
 */
public class ServiceUninstallerTester
        extends ServiceInstallTestBase {

    private final ServiceUninstaller uninstaller;
    private final BundleHome bundleHome;

    public ServiceUninstallerTester(ServiceUninstaller uninstaller) {
        assertNotNull(uninstaller);
        this.uninstaller = uninstaller;

        bundleHome = uninstaller.getBundleHome();
        assertNotNull(bundleHome);
    }

    public void testServiceUninstaller() throws Exception {
        testDummyUninstall();
        testZipUninstall();
    }

    private void testDummyUninstall() throws Exception {
        createDummyFile(bundleHome.getContainer());
        createDummyFile(bundleHome.getAttic());
        BundleCatalog.register(DUMMY_BUNDLE_FILE_NAME);

        verifyDummyBundleInstalled(bundleHome.getContainer(), true);
        verifyDummyBundleInstalled(bundleHome.getAttic(), true);

        uninstaller.uninstall(DUMMY_BUNDLE_FILE_NAME);
        verifyDummyBundleInstalled(bundleHome.getContainer(), false);
        verifyDummyBundleInstalled(bundleHome.getAttic(), false);
    }

    private void createDummyFile(File dir) throws Exception {
        File file = new File(dir, DUMMY_BUNDLE_FILE_NAME);
        BufferedWriter bw =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

        bw.write(DUMMY_BUNDLE_TEXT);
        bw.close();
    }

    private void testZipUninstall() throws Exception {
        createZipBag();
        verifyZipBagInstalled(bundleHome, true);

        uninstaller.uninstall(ZIP_BAG_FILE_NAME);
        verifyZipBagInstalled(bundleHome, false);
    }

    private void createZipBag() throws IOException {
        // Place zip in attic.
        File atticFile = bundleHome.getFromAttic(ZIP_BAG_FILE_NAME);
        FileOutputStream atticStream = FileUtils.openOutputStream(atticFile);
        IOUtils.copy(getZipBag(), atticStream);

        // Place contents of zip in container.
        ZipFile zip = new ZipFile(bundleHome.getFromAttic(ZIP_BAG_FILE_NAME));
        assertTrue(zip.size() > 0);

        Enumeration entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String entryName = entry.getName();
            if (BundleCatalog.register(entryName)) {
                InputStream entryInStream = zip.getInputStream(entry);

                File entryFile = new File(bundleHome.getContainer(), entryName);
                FileOutputStream entryOutStream = FileUtils.openOutputStream(
                    entryFile);

                IOUtils.copy(entryInStream, entryOutStream);

                entryOutStream.close();
                entryInStream.close();
            }
        }

        atticStream.close();
        zip.close();
    }

}
