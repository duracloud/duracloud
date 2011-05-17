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
        assertEquals("2011-05-17T16:01:58.058",
                     DateUtil.convertToStringLong(time));
        assertEquals("2011-05-17T16:01:58",
                     DateUtil.convertToString(time));
        assertEquals("2011-05-17:16:01",
                     DateUtil.convertToStringMid(time));
        assertEquals("2011-05-17", 
                     DateUtil.convertToStringShort(time));

        long roundedTime = 1305662518000L;
        Date dateToCheck =
            DateUtil.convertToDate(DateUtil.convertToStringLong(roundedTime));
        assertEquals(roundedTime, dateToCheck.getTime());
    }
}
