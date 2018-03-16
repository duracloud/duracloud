/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.stream;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * @author dbernstein
 */
public class KnownLengthInputStreamTest {

    @Test
    public void testUTF8StreamLength() throws Exception {
        testString("张大意");
    }

    @Test
    public void testAsciStreamLength() throws Exception {
        testString("abc");
    }

    protected void testString(String string)
        throws IOException, UnsupportedEncodingException {
        int length = string.getBytes(StandardCharsets.UTF_8.name()).length;
        try (KnownLengthInputStream is = new KnownLengthInputStream(string)) {
            assertEquals(length, is.getLength());
        }
    }

}
