/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Andrew Woods
 *         Date: 4/6/12
 */
public class CalendarUtil {

    public static final long ONE_WEEK_MILLIS = 604800000L;

    public static enum DAY_OF_WEEK {
        SUN(Calendar.SUNDAY),
        MON(Calendar.MONDAY),
        TUE(Calendar.TUESDAY),
        WED(Calendar.WEDNESDAY),
        THU(Calendar.THURSDAY),
        FRI(Calendar.FRIDAY),
        SAT(Calendar.SATURDAY);

        private int day;

        DAY_OF_WEEK(int day) {
            this.day = day;
        }
    }

    public Date getDateAtOneAmNext(DAY_OF_WEEK day) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.DAY_OF_WEEK, day.day);
        date.set(Calendar.HOUR, 1);
        date.set(Calendar.AM_PM, Calendar.AM);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        Calendar now = Calendar.getInstance();
        while (date.before(now)) {
            date.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return date.getTime();
    }

}
