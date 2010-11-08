/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mimeutil;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Nov 5, 2010
 */
public class DroidWrapperTest {

    private static File pdfFile0;
    private static File pdfFile1;
    private static File gifFile;
    private static File xmlFile;
    private static File jpgFile;
    private static File mp3File;
    private static File emptyFile;

    private DroidWrapper droid;
    private Map<File, String> fileToMimes;

    @BeforeClass
    public static void beforeClass() {
        File resources = new File("src/test/resources");
        pdfFile0 = new File(resources, "pdf0.file");
        pdfFile1 = new File(resources, "pdf1.file");
        gifFile = new File(resources, "gif.file");
        xmlFile = new File(resources, "xml.file");
        jpgFile = new File(resources, "jpg.file");
        mp3File = new File(resources, "mp3.file");
        emptyFile = new File(resources, "empty.file");
        
        Assert.assertTrue(pdfFile0.getName(), pdfFile0.exists());
        Assert.assertTrue(pdfFile1.getName(), pdfFile1.exists());
        Assert.assertTrue(gifFile.getName(), gifFile.exists());
        Assert.assertTrue(xmlFile.getName(), xmlFile.exists());
        Assert.assertTrue(jpgFile.getName(), jpgFile.exists());
        Assert.assertTrue(mp3File.getName(), mp3File.exists());
        Assert.assertTrue(emptyFile.getName(), emptyFile.exists());
    }

    @Before
    public void setUp() throws Exception {
        droid = new DroidWrapper();
        fileToMimes = new HashMap<File, String>();
        fileToMimes.put(pdfFile0, "application/pdf");
        fileToMimes.put(pdfFile1, "application/pdf");
        fileToMimes.put(gifFile, "image/gif");
        fileToMimes.put(xmlFile, "text/xml");
        fileToMimes.put(jpgFile, "image/jpeg");
        fileToMimes.put(mp3File, "audio/mpeg");
        fileToMimes.put(emptyFile, "application/octet-stream");
    }

    @After
    public void tearDown() throws Exception {
        droid = null;
        fileToMimes = null;
    }

    @Test
    public void testDetermineMimeTypeFile() throws Exception {
        for (File file : fileToMimes.keySet()) {
            String mime = droid.determineMimeType(file);
            Assert.assertNotNull(mime);
            Assert.assertEquals(fileToMimes.get(file), mime);
        }
    }

    @Test
    public void testDetermineMimeTypeStream() throws Exception {
        for (File file : fileToMimes.keySet()) {
            String mime = droid.determineMimeType(getStream(file));            
            Assert.assertNotNull(mime);
            Assert.assertEquals(fileToMimes.get(file), mime);
        }
    }

    private InputStream getStream(File file) throws FileNotFoundException {
        return new AutoCloseInputStream(new FileInputStream(file));
    }

}
