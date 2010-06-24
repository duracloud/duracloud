/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.stream;

import org.apache.commons.io.input.CountingInputStream;
import org.duracloud.common.util.ChecksumUtil;
import static org.duracloud.common.util.ChecksumUtil.Algorithm.MD5;

import java.io.InputStream;
import java.security.DigestInputStream;

/**
 * This class combines the two InputStream implementations:
 * - CountingInputStream &
 * - DigestInputStream
 *
 * @author Andrew Woods
 *         Date: Feb 10, 2010
 */
public class CountingDigestInputStream extends CountingInputStream {

    private boolean preserveMD5;

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
        if (preserveMD5) {
            return ChecksumUtil.getChecksum((DigestInputStream) this.in);
        } else {
            return "MD5-not-preserved";
        }
    }
}
