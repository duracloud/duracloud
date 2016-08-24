/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.apache.commons.io.FileUtils;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.MimetypeUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: 10/21/11
 */
public class MonitoredFileTest {

    private File file;
    private MonitoredFile mFile;

    @Before
    public void setUp() throws Exception {
        file = File.createTempFile("temp", "file");
        mFile = new MonitoredFile(file);
        assertEquals(file.exists(), mFile.exists());

        FileUtils.writeStringToFile(file, "This file is used to execute tests");
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteQuietly(file);
    }

    @Test
    public void testMonitoredFile() throws Exception {
        assertEquals(file.getName(), mFile.getName());
        assertEquals(file.length(), mFile.length());
        assertEquals(file.exists(), mFile.exists());
        assertEquals(file.getAbsolutePath(), mFile.getAbsolutePath());
        assertEquals(file.toURI(), mFile.toURI());

        MimetypeUtil mimeUtil = new MimetypeUtil();
        assertEquals(mimeUtil.getMimeType(file), mFile.getMimetype());

        ChecksumUtil cksumUtil =
            new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        assertEquals(cksumUtil.generateChecksum(file), mFile.getChecksum());

        MonitoredInputStream stream = mFile.getStream();
        assertNotNull(stream);
        assertEquals(0, mFile.getStreamBytesRead());
        stream.read();
        assertEquals(1, mFile.getStreamBytesRead());
    }

}
