/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.repo;

import org.duracloud.account.db.model.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Erik Paulsson
 *         Date: 7/9/13
 */
@Repository(value="userInvitationRepo")
public interface DuracloudUserInvitationRepo extends JpaRepository<UserInvitation, Long> {

    /**
     * This method returns the user invitation which matches the given
     * redemption code
     *
     * @param redemptionCode the unique code used to redeem this invitation
     * @return invitation associated with the given code
     */
    public UserInvitation findByRedemptionCode(String redemptionCode);

    /**
     * This method returns the set of invitations associated with a given
     * DuraCloud account
     *
     * @param id the identifier of the DuraCloud account
     * @return set of outstanding invitations for the given account
     */
    public List<UserInvitation> findByAccountId(Long id);
}
