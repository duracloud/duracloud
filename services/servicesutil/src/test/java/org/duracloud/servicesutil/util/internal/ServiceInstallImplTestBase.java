/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.internal;

import junit.framework.Assert;
import org.duracloud.servicesutil.util.catalog.BundleCatalog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Andrew Woods
 *         Date: Dec 13, 2009
 */
public class ServiceInstallImplTestBase {

    protected final String ATTIC = "attic" + File.separator;
    protected final String CONTAINER = "container" + File.separator;
    protected final String WORK = "work" + File.separator;

    protected final String name = "service-x";

    protected final String nameTxt = name + ".txt";
    protected final String nameJar = name + ".jar";
    protected final String nameZip = name + ".zip";

    protected final String serviceContent0 = "not-much-content";
    protected final String serviceContent1 = "not-much-more-content";
    protected final String serviceContent2 = "not-much-more-content-war";

    protected final String entryName0 = "entry-name.jar";
    protected final String entryName1 = "entry-name.txt";
    protected final String entryName2 = "entry-name.war";

    protected void tearDown() throws Exception {
        BundleCatalog.clearCatalog();
    }

    protected InputStream createBagArchive(File file) throws Exception {
        file.deleteOnExit();

        ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(
            file));
        addEntry(zipOutput, entryName0, serviceContent0);
        addEntry(zipOutput, entryName1, serviceContent1);
        addEntry(zipOutput, entryName2, serviceContent2);

        zipOutput.close();

        return new FileInputStream(file.getPath());
    }

    private void addEntry(ZipOutputStream zipOutput,
                          String entryName,
                          String content) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zipOutput.putNextEntry(entry);
        zipOutput.write(content.getBytes());
        zipOutput.closeEntry();
    }

    protected void verifyExists(boolean exists, File file) {
        Assert.assertNotNull(file);
        Assert.assertEquals(exists, file.exists());
    }

}
