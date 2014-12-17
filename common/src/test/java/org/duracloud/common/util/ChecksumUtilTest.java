/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.duracloud.common.util.ChecksumUtil.Algorithm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.security.DigestInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ChecksumUtilTest {

    private String content;
    private String shortContent = "Test Content";
    private String shortContentMd5Hex = "d65cdbadce081581e7de64a5a44b4617";
    private String shortContentMd5Base64 = "1lzbrc4IFYHn3mSlpEtGFw==";

    private InputStream stream;

    @Before
    public void setUp() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8000; ++i) {
            sb.append("1234567890123456");
            sb.append("abcdefghijklmnop");
        }
        content = sb.toString();
    }

    @After
    public void tearDown() throws Exception {
        content = null;
        if (stream != null) {
            stream.close();
            stream = null;
        }
    }

    @Test
    public void testGenerateChecksum() {
        ChecksumUtil util;

        util = new ChecksumUtil(Algorithm.MD2);
        String md2 = util.generateChecksum(getStream(content));

        util = new ChecksumUtil(Algorithm.MD5);
        String md5 = util.generateChecksum(getStream(content));

        util = new ChecksumUtil(Algorithm.SHA_1);
        String sha1 = util.generateChecksum(getStream(content));

        util = new ChecksumUtil(Algorithm.SHA_256);
        String sha256 = util.generateChecksum(getStream(content));

        util = new ChecksumUtil(Algorithm.SHA_384);
        String sha384 = util.generateChecksum(getStream(content));

        util = new ChecksumUtil(Algorithm.SHA_512);
        String sha512 = util.generateChecksum(getStream(content));

        assertNotNull(md2);
        assertNotNull(md5);
        assertNotNull(sha1);
        assertNotNull(sha256);
        assertNotNull(sha384);
        assertNotNull(sha512);

        boolean diff0 =
                (md2 != md5 && md2 != sha1 && md2 != sha256 && md2 != sha384 && md2 != sha512);

        boolean diff1 =
                (md5 != sha1 && md5 != sha256 && md5 != sha384 && md5 != sha256);

        boolean diff2 = (sha1 != sha256 && sha1 != sha384 && sha1 != sha512);

        boolean diff3 = (sha256 != sha384 && sha256 != sha512);

        boolean diff4 = (sha384 != sha512);

        assertTrue(diff0);
        assertTrue(diff1);
        assertTrue(diff2);
        assertTrue(diff3);
        assertTrue(diff4);
    }

    private InputStream getStream(String data) {
        stream = new ByteArrayInputStream(data.getBytes());
        return stream;
    }

    @Test
    public void testWrapStreamGetChecksum() throws Exception {
        ChecksumUtil util = new ChecksumUtil(Algorithm.MD5);
        String md5 = util.generateChecksum(getStream(content));

        DigestInputStream wrappedStream =
            ChecksumUtil.wrapStream(getStream(content), Algorithm.MD5);

        while(wrappedStream.read() > -1) {
            // Just read through the bytes
        }

        String checksum = ChecksumUtil.getChecksum(wrappedStream);
        assertEquals(md5, checksum);
    }

    @Test
    public void testGetChecksumBytes() throws Exception {
        ChecksumUtil util = new ChecksumUtil(Algorithm.MD5);
        String md5 = util.generateChecksum(getStream(content));

        DigestInputStream wrappedStream =
            ChecksumUtil.wrapStream(getStream(content), Algorithm.MD5);

        while(wrappedStream.read() > -1) {
            // Just read through the bytes
        }

        byte[] checksumBytes = ChecksumUtil.getChecksumBytes(wrappedStream);
        String checksum = ChecksumUtil.checksumBytesToString(checksumBytes);
        assertEquals(md5, checksum);
    }

    @Test
    public void testHexStringToByteArray() throws Exception {
        ChecksumUtil util = new ChecksumUtil(Algorithm.MD5);
        String md5 = util.generateChecksum(getStream(content));

        byte[] checksumBytes = ChecksumUtil.hexStringToByteArray(md5);
        String checksum = ChecksumUtil.checksumBytesToString(checksumBytes);

        assertEquals(md5, checksum);
    }

    @Test
    public void testGetFileChecksum() throws Exception {
        File tempFile = File.createTempFile("checksum-util-test", "file");
        Writer writer = new FileWriter(tempFile);
        writer.write(content);
        writer.close();

        ChecksumUtil util = new ChecksumUtil(Algorithm.MD5);
        try {
            String fileMd5 = util.generateChecksum(tempFile);
            String streamMd5 = util.generateChecksum(getStream(content));
            assertEquals(fileMd5, streamMd5);
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void testGenerateChecksumBase64() throws Exception {
        ChecksumUtil util = new ChecksumUtil(Algorithm.MD5);
        String base64Checksum = util.generateChecksumBase64(shortContent);

        assertEquals(shortContentMd5Base64, base64Checksum);
    }

    @Test
    public void testGenerateChecksumString() throws Exception {
        ChecksumUtil util = new ChecksumUtil(Algorithm.MD5);
        String md5hex = util.generateChecksum(shortContent);
        assertEquals(shortContentMd5Hex, md5hex);

        String md5Base64 = ChecksumUtil.convertToBase64Encoding(md5hex);
        assertEquals(shortContentMd5Base64, md5Base64);

        // Compare string generate with stream generate
        util = new ChecksumUtil(Algorithm.SHA_256);
        String stream = util.generateChecksum(getStream(content));
        String string = util.generateChecksum(content);
        assertEquals(stream, string);
    }


}
