/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.duracloud.common.util.ChecksumUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Andrew Woods
 *         Date: Oct 14, 2010
 */
public class FileCopierTest {

    private FileCopier copier;

    private File testDir = new File("target", "test-copier");

    private File localFile = new File(testDir, "localFile.txt");
    private File remoteFile = new File(testDir, "remoteFile.txt");
    private boolean toLocal;

    @Before
    public void setUp() {
        if (!testDir.exists()) {
            Assert.assertTrue(testDir.mkdir());
        }
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testRunToLocal() throws Exception {
        setUpToLocal();
        copier.run();
        copier.run();
    }

    private void setUpToLocal() throws Exception {
        createContent(remoteFile);
        Path remotePath = new Path(remoteFile.getAbsolutePath());
        toLocal = true;

        copier = new TestFileCopier(localFile, remotePath, toLocal, remoteFile);
    }

    private void createContent(File file) throws IOException {
        OutputStream out = FileUtils.openOutputStream(file);
        IOUtils.write("hello", out);
        IOUtils.closeQuietly(out);
    }

    @Test
    public void testRunFromLocal() throws Exception {
        setUpFromLocal();
        copier.run();
    }

    private void setUpFromLocal() throws IOException {
        createContent(localFile);
        Path remotePath = new Path(remoteFile.getAbsolutePath());
        toLocal = false;

        copier = new FileCopier(localFile, remotePath, toLocal);
    }

    private class TestFileCopier extends FileCopier {
        File file;

        public TestFileCopier(File localFile,
                              Path remotePath,
                              boolean toLocal,
                              File file) {
            super(localFile, remotePath, toLocal);
            this.file = file;
        }

        @Override
        protected String getMd5FromProperties() throws IOException {
            ChecksumUtil checksumUtil =
                new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
            return checksumUtil.generateChecksum(file);
        }
    }

}
