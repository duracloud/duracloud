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

import org.duracloud.mill.db.repo.JpaSpaceStatsRepo;
import org.duracloud.reportdata.storage.SpaceStatsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Daniel Bernstein
 *
 */
@Component("storageStatsResource")
public class StorageStatsResource {

    protected static final Logger log = LoggerFactory.getLogger(SpaceResource.class);

    private JpaSpaceStatsRepo spaceStatsRepo;
    
    @Autowired
    public StorageStatsResource(JpaSpaceStatsRepo spaceStatsRepo){
        this.spaceStatsRepo = spaceStatsRepo;
    }
    
    public List<SpaceStatsDTO> getSpaceStats(
                                                             String accountId,
                                                             String storeId,
                                                             String spaceId,
                                                             Date start,
                                                             Date end) {
        
        
        String interval = getInterval(start, end);
        List<Object[]> list = this.spaceStatsRepo.getByAccountIdAndStoreIdAndSpaceId(accountId, storeId, spaceId, start, end, interval);
        List<SpaceStatsDTO> dtos = new ArrayList<>(list.size());
        for(Object[] s : list){
            dtos.add(new SpaceStatsDTO(new Date(((BigInteger)s[0]).longValue()*1000),
                                       s[1].toString(),
                                       s[2].toString(),
                                       s[3].toString(),
                                       ((BigDecimal) s[4]).longValue(),
                                       ((BigDecimal) s[5]).longValue()));
        }
        
        return dtos;
    }

    protected String getInterval(Date start, Date end) {
        double daysBetweenStartAndEnd = (end.getTime()-start.getTime())/(24*60*60*1000);
        
        String interval = JpaSpaceStatsRepo.INTERVAL_DAY;
        if(daysBetweenStartAndEnd > 120){
            interval = JpaSpaceStatsRepo.INTERVAL_WEEK;
        }else if(daysBetweenStartAndEnd > (2*365)){
            interval = JpaSpaceStatsRepo.INTERVAL_MONTH;
        }
        return interval;
    }

    public List<SpaceStatsDTO> getStorageProviderStats(String account,
                                                       String storeId,
                                                       Date start,
                                                       Date end) {
        String interval = getInterval(start, end);
        List<Object[]> list = this.spaceStatsRepo.getByAccountIdAndStoreId(account, storeId, start, end, interval);
        List<SpaceStatsDTO> dtos = new ArrayList<>(list.size());
        for(Object[] s : list){
            dtos.add(new SpaceStatsDTO(new Date(((BigInteger)s[0]).longValue()*1000),
                                       s[1].toString(),
                                       s[2].toString(),
                                       null,
                                       ((BigDecimal) s[3]).longValue(),
                                       ((BigDecimal) s[4]).longValue()));
        }
        
        return dtos;
    }

//  @Transactional(MillJpaRepoConfig.TRANSACTION_MANAGER_BEAN)
//  public void addSpaceStats(String accountId,
//                            String storeId,
//                            String spaceId, 
//                            long byteCount,
//                            long objectCount){
//     
//      this.spaceStatsRepo.save(new SpaceStats(new Date(), accountId, storeId, spaceId, byteCount, objectCount));
//  }
}
