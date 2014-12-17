/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.awt.SystemColor;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.duracloud.common.util.DateUtil.DateFormat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: 5/17/11
 */
public class DateUtilTest {

    @Test
    public void testDateUtil() throws Exception {
        String now = DateUtil.now();
        String nowLong = DateUtil.nowLong();
        String nowMid = DateUtil.nowMid();
        String nowShort = DateUtil.nowShort();

        assertTrue(nowLong.length() > now.length());
        assertTrue(now.length() > nowMid.length());
        assertTrue(nowMid.length() > nowShort.length());

        long time = 1305662518734L;
        String dateRegex = "2011-05";
        String dayRegex = "-1[7-8]";
        String timeMinRegex = "[0-9][0-9]:[0,3]1";
        String timeSec = ":58";
        String timeSubSec = ".058";

        String longDate = DateUtil.convertToStringLong(time);
        assertTrue(longDate, longDate.matches(
            dateRegex + dayRegex + "T" + timeMinRegex + timeSec + timeSubSec));

        String defaultDate = DateUtil.convertToString(time);
        assertTrue(defaultDate, defaultDate.matches(
            dateRegex + dayRegex + "T" + timeMinRegex + timeSec));

        String medDate = DateUtil.convertToStringMid(time);
        assertTrue(medDate, medDate.matches(
            dateRegex + dayRegex + "T" + timeMinRegex));

        String shortDate = DateUtil.convertToStringShort(time);
        assertTrue(shortDate, shortDate.matches(dateRegex + dayRegex));

        String yearMonthDate = DateUtil.convertToStringYearMonth(time);
        assertTrue(yearMonthDate, yearMonthDate.matches(dateRegex));

        long roundedTime = 1305662518000L;
        Date dateToCheck =
            DateUtil.convertToDate(DateUtil.convertToString(roundedTime));
        assertEquals(roundedTime, dateToCheck.getTime());
    }

    @Test
    public void testConvertToDate() throws Exception {
        String text = "2012-03-29T12:00:00";
        Date date = DateUtil.convertToDate(text);
        Assert.assertNotNull(date);
    }

    @Test
    public void testConvertToDateVerbose() throws Exception {
        String text = "Wed, 21 Mar 2012 02:06:21 UTC";
        Date date = DateUtil.convertToDate(text, DateFormat.VERBOSE_FORMAT);
        Assert.assertNotNull(date);
    }

    @Test
    public void testConvertToDatePlain() throws Exception {
        String text = "2012-03-29-12-00-00";
        Date date = DateUtil.convertToDate(text, DateFormat.PLAIN_FORMAT);
        Assert.assertNotNull(date);
    }
    
    @Test
    public void testProveMultithreadedParsingWorks() throws Exception {
        int count = 100;
        final CountDownLatch latch = new CountDownLatch(count);
        for(int i = 0; i < count; i++){
            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    String date = DateUtil.convertToString(System.currentTimeMillis());
                    try {
                        DateUtil.convertToDate(date);
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail("unexpected failure");
                    }finally{
                        latch.countDown();
                    }
                }
            }).start();
        }
        
        Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

}
