/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.fixity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.services.hadoop.base.ProcessResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Andrew Woods
 *         Date: Sep 23, 2010
 */
public class HashFinderMapperTest {

    private HashFinderMapper mapper;

    private File testDir = new File("target", "test-hash-mapper");
    private File file = new File(testDir, "file.txt");

    private String contentId = "dir0/dir1/content.txt";

    private String hash;

    @Before
    public void setUp() throws Exception {
        mapper = new HashFinderMapper();

        if (!testDir.exists()) {
            Assert.assertTrue(testDir.mkdir());
        }
        createContent(file);
        hash = getHash(file);
    }

    private void createContent(File file) throws IOException {
        OutputStream outStream = FileUtils.openOutputStream(file);
        IOUtils.write("hello", outStream);
        IOUtils.closeQuietly(outStream);
    }

    private String getHash(File file) throws IOException {
        ChecksumUtil cksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        return cksumUtil.generateChecksum(file);
    }

    @Test
    public void testProcessFileNull() throws IOException {
        ProcessResult processResult = mapper.processFile(null, contentId);
        Assert.assertNull(processResult);

        String result = mapper.collectResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("null-space,null-content-id,null-file", result);
    }

    @Test
    public void testProcessFile() throws IOException {
        ProcessResult processResult = mapper.processFile(file, contentId);
        Assert.assertNull(processResult);

        String result = mapper.collectResult();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.endsWith(hash));
    }

    @Test
    public void testCollectResult() throws IOException {
        String result = mapper.collectResult();
        Assert.assertNotNull(result);
        Assert.assertEquals("null-space,null-content-id,null", result);
    }

}
