/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.duracloud.common.util.CalendarUtil.DAY_OF_WEEK.FRI;

/**
 * @author Andrew Woods
 *         Date: 4/6/12
 */
public class CalendarUtilTest {

    private CalendarUtil calendarUtil;

    @Before
    public void setUp() throws Exception {
        calendarUtil = new CalendarUtil();
    }

    @Test
    public void testGetDateAtOneAmNext() throws Exception {
        Date nextReportDate = calendarUtil.getDateAtOneAmNext(FRI);
        Assert.assertNotNull(nextReportDate);

        Date now = new Date();
        Assert.assertTrue("Next report date should be after the current date",
                          nextReportDate.after(now));
        Assert.assertTrue("The next scheduled date should be within a week",
                          (nextReportDate.getTime() - now.getTime()) <=
                              CalendarUtil.ONE_WEEK_MILLIS);
    }

}
