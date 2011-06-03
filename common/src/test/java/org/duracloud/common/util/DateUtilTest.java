/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.junit.Test;

import java.util.Date;

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
        String dateRegex = "2011-05-1[7-8]";
        String timeMinRegex = "[0-9][0-9]:[0,3]1";
        String timeSec = ":58";
        String timeSubSec = ".058";

        String longDate = DateUtil.convertToStringLong(time);
        assertTrue(longDate, longDate.matches(dateRegex + "T" + timeMinRegex +
                                              timeSec + timeSubSec));

        String defaultDate = DateUtil.convertToString(time);
        assertTrue(defaultDate, defaultDate.matches(dateRegex + "T" +
                                                    timeMinRegex + timeSec));

        String medDate = DateUtil.convertToStringMid(time);
        assertTrue(medDate, medDate.matches(dateRegex + "T" + timeMinRegex));

        String shortDate = DateUtil.convertToStringShort(time);
        assertTrue(shortDate, shortDate.matches(dateRegex));

        long roundedTime = 1305662518000L;
        Date dateToCheck =
            DateUtil.convertToDate(DateUtil.convertToStringLong(roundedTime));
        assertEquals(roundedTime, dateToCheck.getTime());
    }
}
