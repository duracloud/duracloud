/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.internal;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.duracloud.services.common.error.ServiceException;
import org.duracloud.services.common.util.BundleHome;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ServiceInstallerImplTest extends ServiceInstallImplTestBase {

    private ServiceInstallerImpl installer;

    private final InputStream bagTxt =
            new ByteArrayInputStream(serviceContent0.getBytes());

    private InputStream bagJar;
    private InputStream bagZip;

    @Before
    public void setUp() throws Exception {
        installer = new ServiceInstallerImpl();
        installer.setBundleHome(new BundleHome());

        bagJar = createBagJar();
        bagZip = createBagZip();
    }

    private InputStream createBagZip() throws Exception {
        return createBagArchive(File.createTempFile(name, ".zip"));
    }

    private InputStream createBagJar() throws Exception {
        return createBagArchive(File.createTempFile(name, ".jar"));
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        
        FileUtils.deleteQuietly(installer.getBundleHome().getHome());

        if (bagTxt != null) {
            bagTxt.close();
        }
        if (bagJar != null) {
            bagJar.close();
        }
        if (bagZip != null) {
            bagZip.close();
        }
    }

    @Test
    public void testInit() throws Exception {
        String home = installer.getBundleHome().getBaseDir();
        File container = new File(home, CONTAINER);
        File attic = new File(home, ATTIC);
        File work = new File(home, WORK);
        
        installer.init();
        Assert.assertTrue(container.exists());
        Assert.assertTrue(attic.exists());
        Assert.assertTrue(work.exists());
    }

    @Test
    public void testTxtInstall() throws Exception {
        try {
            installer.install(nameTxt, bagTxt);
            Assert.fail("Should throw exception for unsupported file type");
        } catch (ServiceException e) {
        }

        File container = installer.getBundleHome().getContainer();
        verifyExists(false, new File(container, nameTxt));
    }

    @Test
    public void testZipInstall() throws Exception {
        installer.install(nameZip, bagZip);

        File container = installer.getBundleHome().getContainer();
        File attic = installer.getBundleHome().getAttic();
        File workDir = installer.getBundleHome().getWork();

        File atticBag = new File(attic, nameZip);
        verifyBag(atticBag);

        File bundle = new File(container, entryName0);
        verifyBundle(bundle, serviceContent0);

        File noBundle = new File(container, entryName1);
        verifyExists(false, noBundle);

        File serviceWorkDir = new File(workDir, name); // no extension
        File war = new File(serviceWorkDir, entryName2);
        verifyBundle(war, serviceContent2);
    }

    @Test
    public void testJarInstall() throws Exception {
        installer.install(nameJar, bagJar);

        BundleHome bundleHome = installer.getBundleHome();
        File installedBundle = bundleHome.getFromAttic(nameJar);
        verifyBag(installedBundle);

        File bundle = bundleHome.getFromContainer(nameJar);
        verifyExists(true, bundle);
    }

    private void verifyBag(File bundle) throws Exception {
        verifyExists(true, bundle);

        ZipFile file = new ZipFile(bundle);
        verifyEntry(file, entryName0, serviceContent0);
        verifyEntry(file, entryName1, serviceContent1);
    }

    private void verifyEntry(ZipFile file, String entryName, String content)
            throws Exception, IOException {
        ZipEntry entry = file.getEntry(entryName);
        Assert.assertNotNull(entry);
        Assert.assertEquals(entryName, entry.getName());

        verifyContent(file.getInputStream(entry), content);
    }

    private void verifyBundle(File bundle, String content) throws Exception {
        verifyExists(true, bundle);
        verifyContent(new FileInputStream(bundle), content);
    }

    private void verifyContent(InputStream inStream, String content)
            throws Exception {
        InputStreamReader sr = new InputStreamReader(inStream);
        BufferedReader br = new BufferedReader(sr);

        String line = br.readLine();
        StringBuilder contentRead = new StringBuilder();
        while (line != null) {
            contentRead.append(line);
            line = br.readLine();
        }

        Assert.assertEquals(content, contentRead.toString());
    }
}
