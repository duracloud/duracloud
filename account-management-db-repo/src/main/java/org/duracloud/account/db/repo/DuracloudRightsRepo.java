/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.repo;

import org.duracloud.account.db.model.AccountRights;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Erik Paulsson
 *         Date: 7/8/13
 */
@Repository(value="rightsRepo")
public interface DuracloudRightsRepo extends JpaRepository<AccountRights, Long>{

    /**
     * This method returns the set of rights for a given account
     * The list may be of 0 length
     *
     * @param accountId of account
     * @return set of rights
     */
    public List<AccountRights> findByAccountId(Long accountId);

    /**
     * This method returns the set of rights for a given user
     * The list may be of 0 length
     *
     * @param userId of User
     * @return set of rights
     */
    public List<AccountRights> findByUserId(Long userId);

    /**
     * This method returns the set of rights for a given user in a given account
     *
     * @param accountId
     * @param userId
     * @return rights
     */
    public AccountRights findByAccountIdAndUserId(Long accountId,
                                              Long userId);


}
