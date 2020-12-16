/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    private DateUtil() {
        // Ensures no instances are made of this class, as there are only static members.
    }

    public enum DateFormat {
        LONG_FORMAT("yyyy-MM-dd'T'HH:mm:ss.sss"),
        DEFAULT_FORMAT("yyyy-MM-dd'T'HH:mm:ss"),
        MID_FORMAT("yyyy-MM-dd'T'HH:mm"),
        SHORT_FORMAT("yyyy-MM-dd"),
        YEAR_MONTH_FORMAT("yyyy-MM"),
        VERBOSE_FORMAT("EEE, d MMM yyyy HH:mm:ss z"),
        PLAIN_FORMAT("yyyy-MM-dd-HH-mm-ss");

        private final SimpleDateFormat format;

        DateFormat(String pattern) {
            this.format = new SimpleDateFormat(pattern, Locale.ENGLISH);
            this.format.setLenient(false);
        }

        public String getPattern() {
            return format.toPattern();
        }
    }

    public static Date convertToDate(String text, DateFormat format)
        throws ParseException {
        SimpleDateFormat dateFormat = format.format;

        synchronized (dateFormat) {
            return dateFormat.parse(text);
        }
    }

    public static Date convertToDate(String text) throws ParseException {
        return convertToDate(text, DateFormat.DEFAULT_FORMAT);
    }

    public static String now() {
        long now = System.currentTimeMillis();
        return convertToString(now, DateFormat.DEFAULT_FORMAT);
    }

    public static String nowLong() {
        long now = System.currentTimeMillis();
        return convertToString(now, DateFormat.LONG_FORMAT);
    }

    public static String nowMid() {
        long now = System.currentTimeMillis();
        return convertToString(now, DateFormat.MID_FORMAT);
    }

    public static String nowShort() {
        long now = System.currentTimeMillis();
        return convertToString(now, DateFormat.SHORT_FORMAT);
    }

    public static String nowVerbose() {
        long now = System.currentTimeMillis();
        return convertToString(now, DateFormat.VERBOSE_FORMAT);
    }

    public static String nowPlain() {
        long now = System.currentTimeMillis();
        return convertToString(now, DateFormat.PLAIN_FORMAT);
    }

    public static String convertToString(long millis, DateFormat format) {
        SimpleDateFormat dateFormat = format.format;
        synchronized (dateFormat) {
            return dateFormat.format(millis);
        }
    }

    public static String convertToString(long millis) {
        return convertToString(millis, DateFormat.DEFAULT_FORMAT);
    }

    public static String convertToStringLong(long millis) {
        return convertToString(millis, DateFormat.LONG_FORMAT);
    }

    public static String convertToStringMid(long millis) {
        return convertToString(millis, DateFormat.MID_FORMAT);
    }

    public static String convertToStringShort(long millis) {
        return convertToString(millis, DateFormat.SHORT_FORMAT);
    }

    public static String convertToStringVerbose(long millis) {
        return convertToString(millis, DateFormat.VERBOSE_FORMAT);
    }

    public static String convertToStringPlain(long millis) {
        return convertToString(millis, DateFormat.PLAIN_FORMAT);
    }

    public static String convertToStringYearMonth(long millis) {
        return convertToString(millis, DateFormat.YEAR_MONTH_FORMAT);
    }

}
