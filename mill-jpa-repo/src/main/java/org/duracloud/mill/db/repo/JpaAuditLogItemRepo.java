/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mill.db.repo;

import java.util.List;

import org.duracloud.mill.db.model.JpaAuditLogItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author Daniel Bernstein
 *
 */
@Repository(value="auditLogItemRepo")
public interface JpaAuditLogItemRepo extends JpaRepository<JpaAuditLogItem, Long> {

    public Page<JpaAuditLogItem> findByAccountAndStoreIdAndSpaceIdAndContentIdOrderByContentIdAsc(
            @Param("account") String account,
            @Param("storeId") String storeId,
            @Param("spaceId") String spaceId,
            @Param("spaceId") String contentId,
            Pageable pageable);

    public List<JpaAuditLogItem> findTop10000ByWrittenFalseOrderByTimestampAsc();
    
    
    public Long deleteByWrittenTrueAndTimestampLessThan(long timestamp);
    
    /**
     * @param account
     * @param storeId
     * @param spaceId
     * @param contentId
     * @return
     */
    public List<JpaAuditLogItem>
            findByAccountAndStoreIdAndSpaceIdAndContentIdOrderByTimestampDesc(String account,
                                                                              String storeId,
                                                                              String spaceId,
                                                                              String contentId);

    public Page<JpaAuditLogItem> findByAccountAndSpaceIdOrderByTimestampAsc(String account,
                                                           String spaceId,
                                                           Pageable pageable);

}
