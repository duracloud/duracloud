/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model.util;

import java.util.Set;

import org.duracloud.storage.domain.StorageProviderType;

/**
 * Bean to be used for collecting information from users in order to create
 * new DuraCloud accounts.
 *
 * @author: Bill Branan
 * Date: 3/25/11
 */
public class AccountCreationInfo {
    /*
     * The subdomain of duracloud.org which will be used to access the instance
     * associated with this account
     */
    private String subdomain;

    /*
     * The display name of the account
     */
    private String acctName;

    /*
     * The name of the organization responsible for the content in this account
     */
    private String orgName;

    /*
     * The name of the department (if applicable) of the organization
     * responsible for the content in this account
     */
    private String department;

    /*
     * The type of storage provider which will act as primary storage.
     */
    private StorageProviderType primaryStorageProviderType;

    /*
     * The types of storage providers which should be part of this account as
     * secondary storage.
     */
    private Set<StorageProviderType> secondaryStorageProviderTypes;

    public AccountCreationInfo(String subdomain,
                               String acctName,
                               String orgName,
                               String department,
                               StorageProviderType primaryStorageProviderType,
                               Set<StorageProviderType> secondaryStorageProviderTypes) {
        this.subdomain = subdomain;
        this.acctName = acctName;
        this.orgName = orgName;
        this.department = department;
        this.primaryStorageProviderType = primaryStorageProviderType;
        this.secondaryStorageProviderTypes = secondaryStorageProviderTypes;
    }

    public String getSubdomain() {
        return subdomain;
    }

    public String getAcctName() {
        return acctName;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getDepartment() {
        return department;
    }

    public StorageProviderType getPrimaryStorageProviderType() {
        return primaryStorageProviderType;
    }

    public Set<StorageProviderType> getSecondaryStorageProviderTypes() {
        return secondaryStorageProviderTypes;
    }

}
