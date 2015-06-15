/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.repo;

import org.duracloud.account.db.model.DuracloudMill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Daniel Bernstein
 *         Date: 05/06/2015
 */
@Repository(value="duracloudMillRepo")
public interface DuracloudMillRepo extends JpaRepository<DuracloudMill, Long> {
    
}
