/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil.internal;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.duracloud.services.common.error.ServiceException;
import org.duracloud.services.common.model.NamedFilterList;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Andrew Woods
 *         Date: Jan 28, 2010
 */
public class FilteredWarTest extends TestCase {

    private NamedFilterList filterList;
    private FilteredWar war;

    private File resourceDir = new File("src/test/resources");
    private String warName = "filteredWarTest.war";

    private File outputDir = new File("target/test-filtered-war");

    private String file0 = "viewer.html";
    private String file1 = "index.html";
    private String file2 = "WEB-INF/classes/djatoka.properties";

    private String target0a = "$DURA_HOST$";
    private String target0b = "$DURA_PORT$";
    private String target1 = "$DURA_TITLE$";
    private String target2 = "$DURA_PROP$";

    private String value0a = "test.duracloud.org";
    private String value0b = "12345";
    private String value1 = "DuraCloud Title";
    private String value2 = "prop=true";

    private long sizeFile0;
    private long sizeFile1;
    private long sizeFile2;
    private long newLineChar = 1;

    private long sizeDiff0 = (value0a.length() + value0b.length()) -
        (target0a.length() + target0b.length()) - newLineChar;
    private long sizeDiff1 = value1.length() - target1.length() - newLineChar;
    private long sizeDiff2 = value2.length() - target2.length() - newLineChar;

    public void setUp() throws ServiceException {
        Map<String, String> filters0 = new HashMap<String, String>();
        Map<String, String> filters1 = new HashMap<String, String>();
        Map<String, String> filters2 = new HashMap<String, String>();

        filters0.put(target0a, value0a);
        filters0.put(target0b, value0b);
        filters1.put(target1, value1);
        filters2.put(target2, value2);

        NamedFilterList.NamedFilter namedFilter0 = new NamedFilterList.NamedFilter(
            file0,
            filters0);
        NamedFilterList.NamedFilter namedFilter1 = new NamedFilterList.NamedFilter(
            file1,
            filters1);
        NamedFilterList.NamedFilter namedFilter2 = new NamedFilterList.NamedFilter(
            file2,
            filters2);

        List<NamedFilterList.NamedFilter> namedFilters = new ArrayList<NamedFilterList.NamedFilter>();
        namedFilters.add(namedFilter0);
        namedFilters.add(namedFilter1);
        namedFilters.add(namedFilter2);
        filterList = new NamedFilterList(namedFilters);

    }

    @Test
    public void testFiltering() throws IOException {
        boolean isFiltered = true;
        verifyWar(getWar(), !isFiltered);
        verifyWar(new FilteredWar(getWar(), filterList), isFiltered);
    }

    private void verifyWar(InputStream war, boolean isFiltered)
        throws IOException {
        String dirName = "initial";
        if (isFiltered) {
            dirName = "filtered";
        }
        File dir = getOutputDir(dirName);
        unZip(war, dir);

        File expected0 = new File(dir, file0);
        File expected1 = new File(dir, file1);
        File expected2 = new File(dir, file2);

        if (!isFiltered) {
            sizeFile0 = expected0.length();
            sizeFile1 = expected1.length();
            sizeFile2 = expected2.length();
        } else {
            Assert.assertEquals(sizeFile0 + sizeDiff0, expected0.length());
            Assert.assertEquals(sizeFile1 + sizeDiff1, expected1.length());
            Assert.assertEquals(sizeFile2 + sizeDiff2, expected2.length());
        }

        Assert.assertTrue(expected0.getAbsolutePath(), expected0.exists());
        Assert.assertTrue(expected1.getAbsolutePath(), expected1.exists());
        Assert.assertTrue(expected2.getAbsolutePath(), expected2.exists());

        verifyContains(expected0, target0a, !isFiltered);
        verifyContains(expected0, target0b, !isFiltered);
        verifyContains(expected0, value0a, isFiltered);
        verifyContains(expected0, value0b, isFiltered);
        verifyContains(expected1, target1, !isFiltered);
        verifyContains(expected1, value1, isFiltered);
        verifyContains(expected2, target2, !isFiltered);
        verifyContains(expected2, value2, isFiltered);

    }

    private void verifyContains(File file, String text, boolean expected)
        throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));

        boolean found = false;
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains(text)) {
                found = true;
            }
        }
        Assert.assertEquals(text, found, expected);
    }

    private void unZip(InputStream war, File dir) throws IOException {
        File warFile = new File(dir, "file.war");
        FileOutputStream outputWar = new FileOutputStream(warFile);
        IOUtils.copy(war, outputWar);
        outputWar.close();

        final int BUFFER = 2048;
        BufferedOutputStream dest = null;
        FileInputStream fis = new FileInputStream(warFile);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {

            if (entry.isDirectory()) {
                new File(dir, entry.getName()).mkdirs();
            } else {
                int count;
                byte data[] = new byte[BUFFER];
                // write the files to the disk
                FileOutputStream fos = new FileOutputStream(getFile(dir,
                                                                    entry.getName()));
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
        }
        zis.close();

    }

    private InputStream getWar() throws FileNotFoundException {
        File warFile = new File(resourceDir, warName);
        if (!warFile.exists()) {
            throw new FileNotFoundException(warFile.getAbsolutePath());
        }
        return new FileInputStream(warFile);
    }

    private File getOutputDir(String dirName) {
        File dir = new File(outputDir, dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private File getFile(File dir, String fileName) {
        File file = new File(dir, fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

}
