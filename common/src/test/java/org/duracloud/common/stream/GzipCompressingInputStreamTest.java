/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.stream;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * 
 * @author Michael Wyraz
 * @see http://stackoverflow.com/questions/11036280/compress-an-inputstream-with-gzip
 *
 */
public class GzipCompressingInputStreamTest {

    @Test
    public void test() throws Exception {
        testCompressor("test1 test2 test3");
        testCompressor("1MB binary data", createTestPattern(1024 * 1024));
        for (int i = 0; i < 4096; i++) {
            testCompressor(i + " bytes of binary data", createTestPattern(i));
        }
    }

    protected byte[] createTestPattern(int size) {
        byte[] data = new byte[size];
        byte pattern = 0;
        for (int i = 0; i < size; i++) {
            data[i] = pattern++;
        }
        return data;
    }

    protected void testCompressor(String data) throws IOException {
        testCompressor("String: " + data, data.getBytes());
    }

    protected void testCompressor(String dataInfo, byte[] data)
        throws IOException {
        InputStream uncompressedIn = new ByteArrayInputStream(data);
        InputStream compressedIn =
            new GzipCompressingInputStream(uncompressedIn);
        InputStream uncompressedOut = new GZIPInputStream(compressedIn);
        byte[] result = IOUtils.toByteArray(uncompressedOut);

        assertTrue("Test failed for: " + dataInfo, Arrays.equals(data, result));

    }

}