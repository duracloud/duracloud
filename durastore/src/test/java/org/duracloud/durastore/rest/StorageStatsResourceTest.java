/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;

import org.duracloud.mill.db.repo.JpaSpaceStatsRepo;
import org.easymock.Capture;
import org.easymock.Mock;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

/**
 * @author dbernstein
 */
public class StorageStatsResourceTest {

    @Mock
    private JpaSpaceStatsRepo spaceStatsRepo;

    @Test
    public void testGetStorageStats() throws Exception {

        spaceStatsRepo = mock(JpaSpaceStatsRepo.class);
        final String accountId = "account-id";
        final String storeId = "id";
        DateTimeFormatter format = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss z").withZoneUTC();
        final Date date = format.parseDateTime("2019-12-31 12:00:00 UTC").toDate();

        final Capture<Date> captureStart = Capture.newInstance();
        final Capture<Date> captureEnd = Capture.newInstance();

        expect(spaceStatsRepo.getByAccountIdAndStoreIdAndDay(eq(accountId), eq(storeId), capture(captureStart), capture(captureEnd))).andReturn(new ArrayList<>());

        replay(spaceStatsRepo);
        final StorageStatsResource resource = new StorageStatsResource(spaceStatsRepo);
        resource.getStorageProviderByDay(accountId, storeId, date);
        verify(spaceStatsRepo);

        final Date start = captureStart.getValue();
        final Date end = captureEnd.getValue();
        //assertEquals("Start date should be 00:00:00 GMT of the same day as input date", "2019-12-31 00:00:00 UTC", format.print(start.getTime()));
        assertEquals("Start date should be 00:00:00 GMT of the next day as input date", "2019-12-31 00:00:00 UTC", format.print(start.getTime()));
        assertEquals("End date should be 00:00:00 GMT of the next day as input date", "2020-01-01 00:00:00 UTC", format.print(end.getTime()));

    }
}
