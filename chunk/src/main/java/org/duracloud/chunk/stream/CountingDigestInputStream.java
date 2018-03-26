/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.stream;

import static org.duracloud.common.util.ChecksumUtil.Algorithm.MD5;

import java.io.InputStream;
import java.security.DigestInputStream;

import org.apache.commons.io.input.CountingInputStream;
import org.duracloud.common.util.ChecksumUtil;

/**
 * This class combines the two InputStream implementations:
 * - CountingInputStream &
 * - DigestInputStream
 *
 * @author Andrew Woods
 * Date: Feb 10, 2010
 */
public class CountingDigestInputStream extends CountingInputStream {

    private boolean preserveMD5;
    private String md5;

    /**
     * The digest capabilities are turned off if the arg preserveMD5 is false
     *
     * @param inputStream
     * @param preserveMD5
     */
    public CountingDigestInputStream(InputStream inputStream,
                                     boolean preserveMD5) {
        super(wrapStream(inputStream, preserveMD5));
        this.preserveMD5 = preserveMD5;
        this.md5 = null;
    }

    private static InputStream wrapStream(InputStream inputStream,
                                          boolean preserveMD5) {
        InputStream stream = inputStream;
        if (preserveMD5) {
            stream = ChecksumUtil.wrapStream(inputStream, MD5);
        }
        return stream;
    }

    public String getMD5() {
        if (!preserveMD5) {
            md5 = "MD5-not-preserved";

        } else if (null == md5) {
            md5 = ChecksumUtil.getChecksum((DigestInputStream) this.in);
        }
        return md5;
    }
}
