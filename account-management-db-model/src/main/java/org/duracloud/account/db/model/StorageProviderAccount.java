/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model;

import org.duracloud.storage.domain.StorageProviderType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author Erik Paulsson
 *         Date: 7/10/13
 */
@Entity
public class StorageProviderAccount extends ProviderAccount {

    /**
     * The type of storage provider - meaning the organization acting as the
     * provider of storage services.
     */
    @Enumerated(EnumType.STRING)
    private StorageProviderType providerType;

    /**
     * Flag indicating the default storage preference for all content is
     * reduced redundancy (or equivalent lower-priced option at non-Amazon
     * providers.)
     */
    private boolean rrs;

    public StorageProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(StorageProviderType providerType) {
        this.providerType = providerType;
    }

    public boolean isRrs() {
        return rrs;
    }

    public void setRrs(boolean rrs) {
        this.rrs = rrs;
    }
}
