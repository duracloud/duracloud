/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Set;

/**
 * @author Erik Paulsson
 *         Date: 7/10/13
 */
@Entity
public class AccountRights extends BaseEntity {

    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name="account_id", nullable=false, columnDefinition = "bigint(20)")
    private AccountInfo account;

    @ManyToOne(fetch= FetchType.EAGER, optional=true)
    @JoinColumn(name="user_id", nullable=true, columnDefinition = "bigint(20)")
    private DuracloudUser user;

    @ElementCollection(targetClass=Role.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name="account_rights_role",
                     joinColumns=@JoinColumn(name="account_rights_id", columnDefinition = "bigint(20)"))
    @Column(name="role")
    private Set<Role> roles;

    public AccountInfo getAccount() {
        return account;
    }

    public void setAccount(AccountInfo account) {
        this.account = account;
    }

    public DuracloudUser getUser() {
        return user;
    }

    public void setUser(DuracloudUser user) {
        this.user = user;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
