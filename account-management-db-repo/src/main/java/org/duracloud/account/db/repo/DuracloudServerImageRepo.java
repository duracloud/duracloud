/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.repo;

import org.duracloud.account.db.model.ServerImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Erik Paulsson
 *         Date: 7/9/13
 */
@Repository(value="serverImageRepo")
public interface DuracloudServerImageRepo extends JpaRepository<ServerImage, Long> {

    /**
     * Discovers and returns the Server Image which is considered the latest,
     * indicating that it is the preferred image to use when starting a new
     * instance.
     *
     * @return the latest Server Image
     */
    @Query("SELECT si FROM ServerImage si WHERE si.latest = true")
    public ServerImage findLatest();
}
