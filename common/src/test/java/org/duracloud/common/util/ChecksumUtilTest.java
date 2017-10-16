/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.duracloud.common.util.ChecksumUtil.Algorithm;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.DigestInputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void testReusable() throws Exception {
        ChecksumUtil util = new ChecksumUtil(Algorithm.MD5);
        
        String checksum = util.generateChecksum(new ByteArrayInputStream("test".getBytes()));
        
        InputStream is = EasyMock.createMock(InputStream.class);
        EasyMock.expect(is.available()).andReturn(100).anyTimes();
        EasyMock.expect(is.read((byte[])EasyMock.anyObject())).andReturn(100).times(1);
        EasyMock.expect(is.read((byte[])EasyMock.anyObject())).andThrow(new IOException("test exception"));
        EasyMock.replay(is);
        try {
            util.generateChecksum(is);
            Assert.fail("expected previous statement to throw exception");
        }catch(Exception ex){}

        EasyMock.verify(is);

        String checksum2 = util.generateChecksum(new ByteArrayInputStream("test".getBytes()));
        assertEquals("same stream, same checksum util instance should produce same results.",
                     checksum,
                     checksum2);

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

    @Test
    public void testCompareChecksumMethods() throws Exception {
        String data = "Survival’s research"; // Note: the apostrophe in this text is
                                             // unicode U+2019, not a standard apostrophe.

        File tempFile = File.createTempFile("checksum-util-test", "file");
        Writer writer = new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8");
        writer.write(data);
        writer.close();

        ChecksumUtil util = new ChecksumUtil(Algorithm.MD5);
        try {
            String fileMd5 = util.generateChecksum(tempFile);
            System.out.println("File MD5: " + fileMd5);
            assertEquals("c56e8f84a1b33dd5b9caedac1017f1b1", fileMd5);

            String stringMd5 = util.generateChecksum(data);
            System.out.println("String MD5: " + stringMd5);
            assertEquals("c56e8f84a1b33dd5b9caedac1017f1b1", stringMd5);

            assertEquals(fileMd5, stringMd5);
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void testNotThreadSafe() throws Exception {
        byte[] data = new byte[1024*1024];
        ChecksumUtil util = new ChecksumUtil(Algorithm.MD5);
        String checksum = util.generateChecksum(new ByteArrayInputStream(data));
        int count = 40;
        CountDownLatch latch = new CountDownLatch(count);
        AtomicInteger successes = new AtomicInteger(0); 
        for(int i = 0; i < 40; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(util.generateChecksum(new ByteArrayInputStream(data)).equals(checksum)){
                        successes.incrementAndGet();
                    }
                    
                    latch.countDown();

                }
            }).start();
        }
        
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertTrue(count != successes.get());
    }

}
