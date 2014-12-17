/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.repo;

import org.duracloud.account.db.model.AccountInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Erik Paulsson
 *         Date: 7/8/13
 */
@Repository(value="accountRepo")
public interface DuracloudAccountRepo extends JpaRepository<AccountInfo, Long> {

    /**
     * This method returns the account with the given subdomain
     *
     * @param subdomain of account
     * @return account info
     */
    public AccountInfo findBySubdomain(String subdomain);
}
