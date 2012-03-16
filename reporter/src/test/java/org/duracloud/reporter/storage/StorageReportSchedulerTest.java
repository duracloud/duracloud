/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reporter.storage;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author: Bill Branan
 * Date: 6/17/11
 */
public class StorageReportSchedulerTest {

    StorageReportBuilder mockReportBuilder;

    @Before
    public void setup() {
        mockReportBuilder = EasyMock.createMock(StorageReportBuilder.class);
        EasyMock.makeThreadSafe(mockReportBuilder, true);
    }

    private void replayMocks() {
        EasyMock.replay(mockReportBuilder);
    }

    @After
    public void teardown() {
        EasyMock.verify(mockReportBuilder);
    }

    @Test
    public void testStartStorageReport() {
        EasyMock.expect(mockReportBuilder.getStatus())
            .andReturn(StorageReportBuilder.Status.CREATED)
            .anyTimes();

        mockReportBuilder.run();
        EasyMock.expectLastCall()
            .anyTimes();

         replayMocks();

        StorageReportScheduler scheduler =
            new StorageReportScheduler(mockReportBuilder);

        String returnVal = scheduler.startStorageReport();
        assertNotNull(returnVal);
        assertEquals("Storage Report Started", returnVal);
    }

    @Test
    public void testScheduleStorageReport() {
        StorageReportScheduler scheduler =
            new StorageReportScheduler(mockReportBuilder);

        replayMocks();

        // Get next date prior to setting a schedule
        Date nextDate = scheduler.getNextScheduledStartDate();
        assertNull(nextDate);

        // Set the schedule
        int offset = 50000;
        Date time = new Date(System.currentTimeMillis() + offset);
        scheduler.scheduleStorageReport(time, offset);

        // Check the next date
        nextDate = scheduler.getNextScheduledStartDate();
        assertNotNull(nextDate);
        assertEquals(time, nextDate);

        // Cancel the schedule
        scheduler.cancelStorageReportSchedule();
        nextDate = scheduler.getNextScheduledStartDate();
        assertNull(nextDate);
    }

}
