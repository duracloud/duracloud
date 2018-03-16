/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.stream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.duracloud.common.util.ChecksumUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: Apr 14, 2010
 */
public class ChecksumInputStreamTest {

    private InputStream contentStream;
    private String trueChecksum;

    @Before
    public void setUp() throws Exception {
        byte[] content = "test".getBytes();
        contentStream = new ByteArrayInputStream(content);
        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        trueChecksum = util.generateChecksum(contentStream);
        contentStream.reset();
    }

    @After
    public void teardown() throws IOException {
        if (null != contentStream) {
            contentStream.close();
        }
    }

    @Test
    public void testProvideMD5() throws IOException {
        doTest(trueChecksum);
    }

    @Test
    public void testNotProvideMD5() throws IOException {
        doTest(null);
    }

    @Test
    public void testCallTwice() throws IOException {
        ChecksumInputStream stream = new ChecksumInputStream(contentStream,
                                                             null);
        read(stream);
        verifyMD5(stream);
        verifyMD5(stream);
    }

    @Test
    public void testMd5Bytes() throws IOException {
        ChecksumInputStream stream = new ChecksumInputStream(contentStream,
                                                             null);
        read(stream);

        byte[] md5Bytes = stream.getMD5Bytes();
        assertNotNull(md5Bytes);

        String md5 = ChecksumUtil.checksumBytesToString(md5Bytes);
        assertNotNull(md5);

        assertEquals(trueChecksum, md5);
        assertEquals(trueChecksum, stream.getMD5());
    }

    @Test
    public void testMd5BytesProvided() throws IOException {
        ChecksumInputStream stream = new ChecksumInputStream(contentStream,
                                                             trueChecksum);

        byte[] md5Bytes = stream.getMD5Bytes();
        assertNotNull(md5Bytes);

        String md5 = ChecksumUtil.checksumBytesToString(md5Bytes);
        assertNotNull(md5);
        assertEquals(trueChecksum, md5);
        assertEquals(trueChecksum, stream.getMD5());
    }

    private void doTest(String checksum) throws IOException {
        ChecksumInputStream stream =
            new ChecksumInputStream(contentStream, checksum);
        read(stream);
        verifyMD5(stream);
    }

    private void verifyMD5(ChecksumInputStream stream) {
        String md5 = stream.getMD5();
        assertNotNull(md5);
        assertEquals(trueChecksum, md5);
    }

    private void read(InputStream stream) throws IOException {
        while (stream.read() != -1) {
            // spin;
        }
    }
}