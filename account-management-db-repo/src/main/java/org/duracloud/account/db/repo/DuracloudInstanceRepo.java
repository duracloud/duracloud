/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.repo;

import org.duracloud.account.db.model.DuracloudInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Erik Paulsson
 *         Date: 7/8/13
 */
@Repository(value="instanceRepo")
public interface DuracloudInstanceRepo extends JpaRepository<DuracloudInstance, Long> {

    /**
     * This method returns the set of instances associated with a given account
     *
     * @param accountId of account
     * @return set of instance IDs
     */
    public List<DuracloudInstance> findByAccountId(Long accountId);
}
