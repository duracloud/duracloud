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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.duracloud.mill.db.repo.JpaSpaceStatsRepo;
import org.duracloud.reportdata.storage.StoreStatsDTO;
import org.easymock.Capture;
import org.easymock.Mock;
import org.junit.Test;

/**
 * @author dbernstein
 */
public class StorageStatsResourceTest {

    @Mock
    private JpaSpaceStatsRepo spaceStatsRepo;

    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_INSTANT;

    @Test
    public void testGetStorageStats() {
        spaceStatsRepo = mock(JpaSpaceStatsRepo.class);
        final String accountId = "account-id";
        final String storeId = "id";
        final Date date = Date.from(Instant.from(dateTimeFormatter.parse(("2019-12-31T12:00:00Z"))));

        final Capture<Date> captureStart = Capture.newInstance();
        final Capture<Date> captureEnd = Capture.newInstance();

        expect(spaceStatsRepo.getByAccountIdAndStoreIdAndDay(eq(accountId), eq(storeId), capture(captureStart),
                                                             capture(captureEnd))).andReturn(new ArrayList<>());

        replay(spaceStatsRepo);
        final StorageStatsResource resource = new StorageStatsResource(spaceStatsRepo);
        resource.getStorageProviderByDay(accountId, storeId, date);
        verify(spaceStatsRepo);

        final Date start = captureStart.getValue();
        final Date end = captureEnd.getValue();
        //assertEquals("Start date should be 00:00:00 GMT of the same day as input date", "2019-12-31 00:00:00 UTC",
        // format.print(start.getTime()));
        assertEquals("Start date should be 00:00:00 GMT of the next day as input date", "2019-12-31T00:00:00Z",
                     dateTimeFormatter.format(start.toInstant()));
        assertEquals("End date should be 00:00:00 GMT of the next day as input date", "2020-01-01T00:00:00Z",
                     dateTimeFormatter.format(end.toInstant()));

    }
}
