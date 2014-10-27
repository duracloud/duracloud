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
import javax.persistence.OneToMany;

/**
 * @author Erik Paulsson
 *         Date: 7/10/13
 */
@Entity
public class AccountCluster extends BaseEntity {

    private String clusterName;

    /**
     * The IDs of all Accounts which are part of the cluster
     */
    @OneToMany(mappedBy="accountCluster")
    private Set<AccountInfo> clusterAccounts;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Set<AccountInfo> getClusterAccounts() {
        return clusterAccounts;
    }

    public void setClusterAccounts(Set<AccountInfo> clusterAccounts) {
        this.clusterAccounts = clusterAccounts;
    }
}
