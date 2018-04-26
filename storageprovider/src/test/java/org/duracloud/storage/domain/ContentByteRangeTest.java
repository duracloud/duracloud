/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests the Range parsing in the ContentByteRange class
 *
 * @author Bill Branan
 * Date: 4/20/18
 */
public class ContentByteRangeTest {

    @Test
    public void testParseRange() {
        ContentByteRange range;

        // Valid range, 0 to 5
        range = new ContentByteRange("bytes=0-5");
        assertEquals(Long.valueOf(0), range.getRangeStart());
        assertEquals(Long.valueOf(5), range.getRangeEnd());

        // Valid range, 5 to 10
        range = new ContentByteRange("bytes=5-10");
        assertEquals(Long.valueOf(5), range.getRangeStart());
        assertEquals(Long.valueOf(10), range.getRangeEnd());

        // Valid range, 5 to end
        range = new ContentByteRange("bytes=5-");
        assertEquals(Long.valueOf(5), range.getRangeStart());
        assertNull(range.getRangeEnd());

        // Valid range, last 5
        range = new ContentByteRange("bytes=-5");
        assertNull(range.getRangeStart());
        assertEquals(Long.valueOf(5), range.getRangeEnd());

        // Valid range, 5 to 10 (with additional ranges, which are removed)
        range = new ContentByteRange("bytes=5-10,15-20");
        assertEquals(Long.valueOf(5), range.getRangeStart());
        assertEquals(Long.valueOf(10), range.getRangeEnd());

        // Invalid range, missing prefix
        try {
            new ContentByteRange("0-5");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        // Invalid range, non-numeric
        try {
            new ContentByteRange("bytes=one-five");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        // Invalid range, missing range values
        try {
            new ContentByteRange("bytes=-");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        // Invalid range, missing dash
        try {
            new ContentByteRange("bytes=5");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        // Invalid range, empty range
        try {
            new ContentByteRange("");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

}
