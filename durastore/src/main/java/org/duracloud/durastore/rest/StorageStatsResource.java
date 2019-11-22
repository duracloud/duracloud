/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.mill.db.repo.JpaSpaceStatsRepo;
import org.duracloud.reportdata.storage.SpaceStatsDTO;
import org.duracloud.reportdata.storage.StoreStatsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Daniel Bernstein
 */
@Component("storageStatsResource")
public class StorageStatsResource {

    protected static final Logger log = LoggerFactory.getLogger(SpaceResource.class);

    private JpaSpaceStatsRepo spaceStatsRepo;

    final long ONE_DAY_IN_MS = 24 * 60 * 60 * 1000;

    public static enum GroupBy {
        day,
        week,
        month;
    }

    @Autowired
    public StorageStatsResource(JpaSpaceStatsRepo spaceStatsRepo) {
        this.spaceStatsRepo = spaceStatsRepo;
    }

    public List<SpaceStatsDTO> getSpaceStats(String accountId,
                                             String storeId,
                                             String spaceId,
                                             Date start,
                                             Date end,
                                             GroupBy groupBy) {

        String interval = getInterval(groupBy);
        List<Object[]> list =
            this.spaceStatsRepo.getByAccountIdAndStoreIdAndSpaceId(accountId, storeId, spaceId, start, end, interval);
        List<SpaceStatsDTO> dtos = new ArrayList<>(list.size());
        for (Object[] s : list) {
            dtos.add(new SpaceStatsDTO(new Date(((BigInteger) s[0]).longValue() * 1000),
                                       s[1].toString(),
                                       s[2].toString(),
                                       s[3].toString(),
                                       ((BigDecimal) s[4]).longValue(),
                                       ((BigDecimal) s[5]).longValue()));
        }

        return dtos;
    }

    protected String getInterval(GroupBy groupBy) {
        if (groupBy == null) {
            groupBy = GroupBy.day;
        }

        if (groupBy.equals(GroupBy.day)) {
            return JpaSpaceStatsRepo.INTERVAL_DAY;
        } else if (groupBy.equals(GroupBy.week)) {
            return JpaSpaceStatsRepo.INTERVAL_WEEK;
        } else if (groupBy.equals(GroupBy.month)) {
            return JpaSpaceStatsRepo.INTERVAL_MONTH;
        } else {
            throw new DuraCloudRuntimeException("No sql interval defined for groupBy param: " + groupBy);
        }
    }

    public List<StoreStatsDTO> getStorageProviderStats(String account,
                                                       String storeId,
                                                       Date start,
                                                       Date end,
                                                       GroupBy groupBy) {
        String interval = getInterval(groupBy);
        List<Object[]> list = this.spaceStatsRepo.getByAccountIdAndStoreId(account, storeId, start, end, interval);
        List<StoreStatsDTO> dtos = new ArrayList<>(list.size());
        for (Object[] s : list) {
            dtos.add(new StoreStatsDTO(new Date(((BigInteger) s[0]).longValue() * 1000),
                                       s[1].toString(),
                                       s[2].toString(),
                                       ((BigDecimal) s[3]).longValue(),
                                       ((BigDecimal) s[4]).longValue()));
        }

        return dtos;
    }

    public List<SpaceStatsDTO> getStorageProviderByDay(final String account,
                                                       final String storeId,
                                                       final Date date) {

        //Set Range for the entire day
        final long time = date.getTime();
        final long msSoFarToday = time % ONE_DAY_IN_MS;
        final long msLeftToday = ONE_DAY_IN_MS - msSoFarToday;
        final Date start = new Date(time - msSoFarToday);
        //end date should be midnight of the next day
        final Date end = new Date(time + msLeftToday);

        final List<Object[]> list = this.spaceStatsRepo.getByAccountIdAndStoreIdAndDay(account, storeId, start, end);
        final List<SpaceStatsDTO> dtos = new ArrayList<>(list.size());
        for (final Object[] s : list) {
            dtos.add(new SpaceStatsDTO(start,
                s[1].toString(),
                s[2].toString(),
                s[3].toString(),
                ((BigDecimal) s[4]).longValue(),
                ((BigDecimal) s[5]).longValue()));
        }

        return dtos;
    }

}
