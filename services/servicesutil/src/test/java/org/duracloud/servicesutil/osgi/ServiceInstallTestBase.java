/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.osgi;

import static junit.framework.Assert.assertEquals;
import org.duracloud.services.common.util.BundleHome;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Andrew Woods
 *         Date: Oct 1, 2009
 */
public class ServiceInstallTestBase {

    private final Logger log = LoggerFactory.getLogger(ServiceInstallTestBase.class);

    private final static String BASE_DIR_PROP = "base.dir";
    private final static String PROJECT_VERSION_PROP = "PROJECT_VERSION";

    protected final static String DUMMY_BUNDLE_FILE_NAME = "junk-bundle.jar";

    protected final static String DUMMY_BUNDLE_TEXT = "normally-bundle-is-a-jar-not-text";


    protected final static String ZIP_BAG_FILE_NAME = "replicationservice-1.0.0.zip";

    protected final static String sep = File.separator;

    protected InputStream getDummyBundle() throws FileNotFoundException {
        return new ByteArrayInputStream(DUMMY_BUNDLE_TEXT.getBytes());
    }

    protected InputStream getBundleJar() throws FileNotFoundException {
        File bundleFile = new File(getResourceDir(), getBundleJarFilename());
        return new FileInputStream(bundleFile);
    }

    protected String getBundleJarFilename() {
        return "helloservice-" + getVersion() + ".jar";
    }

    private String getVersion() {
        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);
        return version;
    }

    protected InputStream getZipBag() throws FileNotFoundException {
        File zipBagFile = new File(getResourceDir(), ZIP_BAG_FILE_NAME);
        return new FileInputStream(zipBagFile);
    }

    private String getResourceDir() {
        String baseDir = System.getProperty(BASE_DIR_PROP);
        Assert.assertNotNull(baseDir);

        return baseDir + sep + "src/test/resources/";
    }

    protected void verifyDummyBundleInstalled(File dir, boolean exists)
        throws IOException {
        File file = new File(dir, DUMMY_BUNDLE_FILE_NAME);
        assertEquals(exists, file.exists());

        if (exists) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
                file)));
            String line = br.readLine();
            assertEquals(DUMMY_BUNDLE_TEXT, line);
            br.close();
        }
    }

    protected void verifyJarInstalled(BundleHome home, boolean exists) {
        File file = home.getFromContainer(getBundleJarFilename());
        assertEquals(exists, file.exists());
    }

    protected void verifyZipBagInstalled(BundleHome home, boolean exists)
        throws IOException {
        verifyZipBagInstalled(home, ZIP_BAG_FILE_NAME, exists);
    }

    protected void verifyZipBagInstalled(BundleHome home,
                                         String name,
                                         boolean exists)
        throws IOException {
        File file = home.getFromAttic(name);
        assertEquals("file: " + file.getAbsoluteFile(), exists, file.exists());

        if (exists) {
            ZipFile zip = new ZipFile(file);
            assertTrue(zip.size() > 0);

            Enumeration entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File entryBundle = home.getFromContainer(entry.getName());
                assertTrue(entryBundle.exists());
            }
            zip.close();
        }
    }

    protected void deleteDummyBundle(BundleHome home) {
        File file = home.getFromContainer(DUMMY_BUNDLE_FILE_NAME);
        file.delete();

        File atticFile = home.getFromAttic(DUMMY_BUNDLE_FILE_NAME);
        atticFile.delete();
    }

    protected void deleteJarBundle(BundleHome home) {
        File file = home.getFromContainer(getBundleJarFilename());
        file.delete();

        File atticFile = home.getFromAttic(getBundleJarFilename());
        atticFile.delete();
    }

    protected void deleteZipBagBundles(BundleHome home) throws IOException {
        deleteZipBagBundles(home, ZIP_BAG_FILE_NAME);
    }

    protected void deleteZipBagBundles(BundleHome home, String name)
        throws IOException {
        File file = home.getFromAttic(name);

        if (file.exists()) {
            ZipFile zip = new ZipFile(file);

            Enumeration entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File entryBundle = home.getFromContainer(entry.getName());
                entryBundle.delete();
            }
            file.delete();
        }
    }

}
