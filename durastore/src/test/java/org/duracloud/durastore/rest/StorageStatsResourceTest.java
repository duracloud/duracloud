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
import java.time.LocalDate;
import java.time.ZoneOffset;
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

    @Test
    public void testSortsStorageStats() {
        spaceStatsRepo = mock(JpaSpaceStatsRepo.class);
        final var accountId = "account-id";
        final var storeId = "id";
        final var byteCount = new BigDecimal(10000);
        final var objectCount = new BigDecimal(100);

        final Date firstDate = Date.from(Instant.from(dateTimeFormatter.parse(("2019-12-31T12:00:00Z"))));
        final Date lastDate = Date.from(Instant.from(dateTimeFormatter.parse(("2023-12-31T12:00:00Z"))));
        final var now = Date.from(Instant.now());

        // A few quirks to note from the JpaSpaceStatsRepo:
        // - The time returned needs to be divided by 1000. See when creating new StoreStatsDTO/SpaceStatsDTO objects
        // - We get back a BigDecimal for the file and byte counts as they use the aggregate function `avg`
        final var unsorted = List.of(
            List.of(lastDate.getTime() / 1000, accountId, storeId, byteCount, objectCount).toArray(),
            List.of(firstDate.getTime() / 1000, accountId, storeId, byteCount, objectCount).toArray());

        expect(spaceStatsRepo.getByAccountIdAndStoreId(eq(accountId), eq(storeId), eq(firstDate),
                                                       eq(now), eq(JpaSpaceStatsRepo.INTERVAL_DAY)))
            .andReturn(unsorted);

        replay(spaceStatsRepo);
        final var resource = new StorageStatsResource(spaceStatsRepo);
        final List<StoreStatsDTO> storageProviderStats =
            resource.getStorageProviderStats(accountId, storeId, firstDate, now, StorageStatsResource.GroupBy.day);
        verify(spaceStatsRepo);

        assertEquals(2, storageProviderStats.size());
        assertEquals(firstDate, storageProviderStats.get(0).getTimestamp());
        assertEquals(lastDate, storageProviderStats.get(1).getTimestamp());
    }

    @Test
    public void testSortsSpaceStats() {
        spaceStatsRepo = mock(JpaSpaceStatsRepo.class);
        final var accountId = "account-id";
        final var storeId = "id";
        final var spaceId = "space-id";
        final var byteCount = new BigDecimal(10000);
        final var objectCount = new BigDecimal(100);

        final var firstDate = Date.from(Instant.from(dateTimeFormatter.parse("2019-12-31T12:00:00Z")));
        final var lastDate = Date.from(Instant.from(dateTimeFormatter.parse("2023-12-31T12:00:00Z")));
        final var now = Date.from(Instant.now());

        // A few quirks to note from the JpaSpaceStatsRepo:
        // - The time returned needs to be divided by 1000. See when creating new StoreStatsDTO/SpaceStatsDTO objects
        // - We get back a BigDecimal for the file and byte counts as they use the aggregate function `avg`
        // - The final column is the timestamp as a local date format (e.g. 2019-12-31)
        final List<Object[]> unsorted = List.of(
            List.of(lastDate.getTime() / 1000, accountId, storeId, spaceId, byteCount, objectCount,
                    LocalDate.ofInstant(lastDate.toInstant(), ZoneOffset.UTC).toString()).toArray(),
            List.of(firstDate.getTime() / 1000, accountId, storeId, spaceId, byteCount, objectCount,
                    LocalDate.ofInstant(firstDate.toInstant(), ZoneOffset.UTC).toString()).toArray());

        expect(spaceStatsRepo.getByAccountIdAndStoreIdAndSpaceId(eq(accountId), eq(storeId), eq(spaceId),
                                                                 eq(firstDate), eq(now),
                                                                 eq(JpaSpaceStatsRepo.INTERVAL_DAY)))
            .andReturn(unsorted);

        replay(spaceStatsRepo);
        final var resource = new StorageStatsResource(spaceStatsRepo);
        final var storageProviderStats = resource.getSpaceStats(accountId, storeId, spaceId, firstDate, now,
                                                                StorageStatsResource.GroupBy.day);
        verify(spaceStatsRepo);

        assertEquals(2, storageProviderStats.size());
        assertEquals(firstDate, storageProviderStats.get(0).getTimestamp());
        assertEquals(lastDate, storageProviderStats.get(1).getTimestamp());
    }
}
