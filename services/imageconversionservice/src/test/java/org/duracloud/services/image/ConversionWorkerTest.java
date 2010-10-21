/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.image;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: Oct 20, 2010
 */
public class ConversionWorkerTest {

    private File tempDir;
    private ConversionWorker worker;

    @Before
    public void setUp() throws Exception {
        tempDir = new File("target/conversion-worker-test");
        tempDir.mkdir();
        worker = new ConversionWorker(null, null, null, null, tempDir, null,
                                      null, null, null);
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testGetSourceFileName() throws Exception {
        assertEquals("file.txt", worker.getSourceFileName("file.txt"));
        assertEquals("path_to_file.txt",
                     worker.getSourceFileName("path/to/file.txt"));
        assertEquals("path_to_file.txt",
                     worker.getSourceFileName("path\\to\\file.txt"));
        assertEquals("path_to_file_name.txt",
                     worker.getSourceFileName("path/to/file name.txt"));
    }

    @Test
    public void testWriteSourceToFile() throws Exception {
        // test file with simple content id
        InputStream stream = new ByteArrayInputStream("test".getBytes());
        String fileName = "filename.txt";
        File resultFile = worker.writeSourceToFile(stream, fileName);
        assertTrue(resultFile.exists());
        assertEquals(resultFile.getAbsolutePath(),
                     tempDir.getAbsolutePath() + File.separator + fileName);

        // test file with path-based content id
        stream = new ByteArrayInputStream("test".getBytes());
        String origFileName = "path/to/file.txt";
        fileName = worker.getSourceFileName(origFileName);
        assertEquals(fileName, origFileName.replace("/", "_"));

        resultFile = worker.writeSourceToFile(stream, fileName);
        assertTrue(resultFile.exists());
        assertEquals(resultFile.getAbsolutePath(),
                     tempDir.getAbsolutePath() + File.separator + fileName);
        
    }

    @Test
    public void testGetDestContentId() throws Exception {
        // test file with simple content id
        String contentId = "image.jpg";
        String destFileName = "image.png";
        File destFile = new File(tempDir, destFileName);

        String destContentId =
            worker.getDestContentId(contentId, destFile);
        assertEquals(destFileName, destContentId);

        // test file with path-based content id
        String sourcePath = "path/to/";
        String sourceContentId = sourcePath + contentId;
        File fileDir = new File(tempDir, sourcePath);
        destFile = new File(fileDir, destFileName);

        destContentId =
            worker.getDestContentId(sourceContentId, destFile);
        assertEquals(sourcePath + destFileName, destContentId);
    }

}
