/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.stream;

import java.io.ByteArrayInputStream;

/**
 * This wraps ByteArrayInputStream and adds a length field.
 *
 * @author Andrew Woods
 *         Date: Feb 10, 2010
 */
public class KnownLengthInputStream extends ByteArrayInputStream {

    private int length;

    public KnownLengthInputStream(String content) {
        super(content.getBytes());
        this.length = content.length();
    }

    public int getLength() {
        return length;
    }
}
