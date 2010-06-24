/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.stream;

import org.duracloud.common.util.ChecksumUtil;
import static org.duracloud.common.util.ChecksumUtil.Algorithm.MD5;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;

/**
 * @author Andrew Woods
 *         Date: Feb 10, 2010
 */
public class CountingDigestInputStreamTest {

    private CountingDigestInputStream stream;

    @Test
    public void testPreserveMD5() throws IOException {
        doTest(true);
    }

    @Test
    public void testNotPreserveMD5() throws IOException {
        doTest(false);
    }

    private void doTest(boolean preserveMD5) throws IOException {
        InputStream content = createContentStream();
        stream = new CountingDigestInputStream(content, preserveMD5);
        read(stream);

        String md5 = stream.getMD5();
        Assert.assertNotNull(md5);

        String realMD5 = getContentMD5();

        if (preserveMD5) {
            Assert.assertEquals(realMD5, md5);
        } else {
            Assert.assertEquals("MD5-not-preserved", md5);
        }
    }

    private String getContentMD5() throws IOException {
        DigestInputStream digestStream = ChecksumUtil.wrapStream(
            createContentStream(),
            MD5);
        read(digestStream);
        return ChecksumUtil.getChecksum(digestStream);
    }

    private InputStream createContentStream() {
        return new ByteArrayInputStream("hello".getBytes());
    }

    private void read(InputStream stream) throws IOException {
        while (stream.read() != -1) {
            // spin;
        }
    }

}
