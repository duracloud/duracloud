/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

import org.apache.commons.lang3.StringUtils;

/**
 * Handles the parsing and validation of HTTP Range headers for Range-based
 * content requests
 *
 * @author Bill Branan
 * Date: 4/20/18
 */
public class ContentByteRange {

    private Long rangeStart = null;
    private Long rangeEnd = null;

    /**
     * Captures the start and end values described in an HTTP Range header
     *
     * @param range HTTP Range header with format 'bytes=X-Y'
     */
    public ContentByteRange(String range) {
        parseRange(range);
    }

    /**
     * Gets the starting point of the byte range (the number preceding the "-"),
     * or null if there is no number preceding the dash
     */
    public Long getRangeStart() {
        return rangeStart;
    }

    /**
     * Gets the ending point of the byte range (the number following the "-"),
     * or null if there is no number following the dash
     */
    public Long getRangeEnd() {
        return rangeEnd;
    }

    /**
     * Parses the Range HTTP header value. Only a single range is supported (others are dropped).
     *
     * @param range Range header included in HTTP request
     * @throws IllegalArgumentException if range value is not valid
     */
    protected void parseRange(String range) {
        String prefix = "bytes=";
        if (!range.startsWith(prefix) || StringUtils.containsNone(range, "-")) {
            throw new IllegalArgumentException(getUsage(range));
        } else {
            // Strip the prefix and drop all but the first range (if there is a list)
            String byteRange = range.substring(prefix.length()).split(",")[0];

            try {
                // Parse out the range values
                setRangeStart(byteRange.substring(0, byteRange.indexOf("-")));
                setRangeEnd(byteRange.substring(byteRange.indexOf("-") + 1, byteRange.length()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(getUsage(range));
            }

            // Verify that there is either a start or end value for the range (or both)
            if (null == getRangeStart() && null == getRangeEnd()) {
                throw new IllegalArgumentException(getUsage(range));
            }
        }
    }

    /**
     * Provides text describing how the Range header value is to be used,
     * and noting that the provided range is invalid.
     *
     * @param range invalid range value
     * @return
     */
    public String getUsage(String range) {
        return "The Range header value, when utilized, must be in the format " +
               "'bytes=X-Y', where X and Y are numerical values. The value provided (" +
               range + ") is not valid.";
    }

    private void setRangeStart(String rangeStart) {
        if (StringUtils.isNotEmpty(rangeStart)) {
            this.rangeStart = Long.valueOf(rangeStart);
        }
    }

    private void setRangeEnd(String rangeEnd) {
        if (StringUtils.isNotEmpty(rangeEnd)) {
            this.rangeEnd = Long.valueOf(rangeEnd);
        }
    }

}
