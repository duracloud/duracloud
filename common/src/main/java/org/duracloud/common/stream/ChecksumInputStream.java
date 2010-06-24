/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.stream;

import org.duracloud.common.util.ChecksumUtil;
import static org.duracloud.common.util.ChecksumUtil.Algorithm.MD5;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;

/**
 * @author: Bill Branan
 * Date: Apr 14, 2010
 */
public class ChecksumInputStream extends FilterInputStream {

    private String providedChecksum;

    public ChecksumInputStream(InputStream inputStream, String checksum) {
        super(wrapStream(inputStream, checksum));
        this.providedChecksum = checksum;
    }

    private static InputStream wrapStream(InputStream inputStream,
                                          String checksum) {
        InputStream stream = inputStream;
        if (checksum == null) {
            stream = ChecksumUtil.wrapStream(inputStream, MD5);
        }
        return stream;
    }

    public String getMD5() {
        if (providedChecksum == null) {
            return ChecksumUtil.getChecksum((DigestInputStream) this.in);
        } else {
            return providedChecksum;
        }
    }
    
}
