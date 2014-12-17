/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.repo;

import org.duracloud.account.db.model.AccountCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Erik Paulsson
 *         Date: 7/8/13
 */
@Repository(value="accountClusterRepo")
public interface DuracloudAccountClusterRepo extends JpaRepository<AccountCluster, Long> {

}
