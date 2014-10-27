/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

/**
 * @author Erik Paulsson
 *         Date: 7/10/13
 */
@Entity
public class DuracloudGroup extends BaseEntity {

    public static final String PREFIX = "group-";
    public static final String PUBLIC_GROUP_NAME = PREFIX + "public";

    private String name;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="group_user",
        joinColumns=@JoinColumn(name="group_id", referencedColumnName="id", columnDefinition = "bigint(20)"),
        inverseJoinColumns=@JoinColumn(name="user_id", referencedColumnName="id", columnDefinition = "bigint(20)"))
    private Set<DuracloudUser> users;

    @ManyToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name="account_id", nullable=false, columnDefinition = "bigint(20)")
    private AccountInfo account;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getPrettyName(){
        return this.name.substring(this.name.indexOf(PREFIX)+PREFIX.length());
    }

    public Set<DuracloudUser> getUsers() {
        return users;
    }

    public void setUsers(Set<DuracloudUser> users) {
        this.users = users;
    }

    public AccountInfo getAccount() {
        return account;
    }

    public void setAccount(AccountInfo account) {
        this.account = account;
    }
}
