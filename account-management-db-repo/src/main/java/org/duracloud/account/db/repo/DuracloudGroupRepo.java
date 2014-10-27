/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.repo;

import org.duracloud.account.db.model.DuracloudGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Erik Paulsson
 *         Date: 7/8/13
 */
@Repository(value="groupRepo")
public interface DuracloudGroupRepo extends JpaRepository<DuracloudGroup, Long> {

    /**
     * This method returns a single group within the given account, with the
     * given groupname.
     *
     * @param name of group
     * @param accountId associated with group
     * @return group
     */
    public DuracloudGroup findByNameAndAccountId(String name, Long accountId);

    /**
     * This method returns all groups within the given account.
     *
     * @param acctId associated with group
     * @return all groups in account
     */
    public List<DuracloudGroup> findByAccountId(Long acctId);
}
