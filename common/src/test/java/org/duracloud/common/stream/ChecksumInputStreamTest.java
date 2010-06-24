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
import org.duracloud.common.util.ChecksumUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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

    @Test
    public void testProvideMD5() throws IOException {
        doTest(trueChecksum);
    }

    @Test
    public void testNotProvideMD5() throws IOException {
        doTest(null);
    }

    private void doTest(String checksum) throws IOException {
        ChecksumInputStream stream =
            new ChecksumInputStream(contentStream, checksum);
        read(stream);

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