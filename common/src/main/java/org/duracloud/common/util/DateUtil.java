/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;

public class DateUtil {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.sss'Z'";

    public static Date convertToDate(String text, String pattern)
        throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setLenient(false);

        Date date = format.parse(text);
        return date;
    }

    public static Date convertToDate(String text) throws ParseException {
        return convertToDate(text, DEFAULT_PATTERN);
    }

    public static String now() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss");
        Date now = new Date(System.currentTimeMillis());
        return dateFormat.format(now);
    }

    public static String nowMid() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
        Date now = new Date(System.currentTimeMillis());
        return dateFormat.format(now);
    }

    public static String nowShort() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date(System.currentTimeMillis());
        return dateFormat.format(now);
    }

}
