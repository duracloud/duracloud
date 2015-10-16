/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the I/O Utilities.
 *
 * @author Bill Branan
 */
public class IOUtilTest {

    protected static final Logger log =
            LoggerFactory.getLogger(SerializationUtilTest.class);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testReadStringFromStream() throws Exception {
        String testValue = "This is a testing string";
        InputStream testStream = new ByteArrayInputStream(testValue.getBytes("UTF-8"));
        String readValue = IOUtil.readStringFromStream(testStream);
        assertTrue(testValue.equals(readValue));
    }

    @Test
    public void testWriteStringToStream() throws Exception {
        String testValue = "This is a testing string";
        InputStream testStream = IOUtil.writeStringToStream(testValue);
        byte[] bytes = new byte[testValue.getBytes().length];
        testStream.read(bytes);
        String readValue = new String(bytes, "UTF-8");
        assertTrue(testValue.equals(readValue));
    }

    @Test
    public void testFileFindReplace() throws Exception {
        File testFile = File.createTempFile("temp", "file");
        try {
            String fileContent = "before $TO_REPLACE after";
            FileUtils.writeStringToFile(testFile, fileContent);

            IOUtil.fileFindReplace(testFile, "$TO_REPLACE", "and");

            String finalContent = FileUtils.readFileToString(testFile);
            assertEquals("before and after", finalContent);
        } finally {
            testFile.delete();
        }
    }

    @Test
    public void testStreamToFileToStream() throws Exception {
        String content = "content";
        InputStream contentStream = IOUtil.writeStringToStream(content);
        File file = IOUtil.writeStreamToFile(contentStream);
        assertNotNull(file);
        assertTrue(file.exists());

        InputStream fileStream = IOUtil.getFileStream(file);
        String finalContent = IOUtil.readStringFromStream(fileStream);
        Assert.assertEquals(content, finalContent);
    }
    
    @Test
    public void testAddFileToZipOutputStream() throws Exception {
        String content = "content";
        InputStream contentStream = IOUtil.writeStringToStream(content);
        File file = IOUtil.writeStreamToFile(contentStream);
        file.deleteOnExit();
        assertNotNull(file);
        assertTrue(file.exists());
        File zipFile = File.createTempFile("test", "zip");
        zipFile.deleteOnExit();
        
        ZipOutputStream zip = new ZipOutputStream(IOUtil.getOutputStream(zipFile));
        IOUtil.addFileToZipOutputStream(file, zip);
        zip.close();
        
        ZipInputStream is = new ZipInputStream( new FileInputStream(zipFile));
        
        ZipEntry entry = is.getNextEntry();
        Assert.assertEquals(file.getName(),entry.getName());
        String finalContent = IOUtil.readStringFromStream(is);
        Assert.assertEquals(content, finalContent);
    }


}
