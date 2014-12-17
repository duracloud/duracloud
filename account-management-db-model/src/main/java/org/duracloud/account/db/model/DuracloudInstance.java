/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * @author Erik Paulsson
 *         Date: 7/11/13
 */
@Entity
public class DuracloudInstance extends BaseEntity {

    public static String PLACEHOLDER_PROVIDER_ID = "TBD";

    /**
     * The Image on which this instance is based.
     */
    @ManyToOne(fetch= FetchType.EAGER, optional=false)
    @JoinColumn(name="image_id", nullable=false, columnDefinition = "bigint(20)")
    private ServerImage image;

    /**
     * The ID of the Account for which this instance was created
     */
    @OneToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name="account_id", nullable=false, columnDefinition = "bigint(20)")
    private AccountInfo account;

    /**
     * The host name at which this instance is available.
     */
    private String hostName;

    /**
     * The identifier value assigned to this machine instance by the compute
     * provider. This ID is used when starting, stopping, or restarting the
     * server on which the DuraCloud software is running.
     */
    private String providerInstanceId;

    /**
     * Indicates if the instance is available.
     */
    private boolean initialized;

    public ServerImage getImage() {
        return image;
    }

    public void setImage(ServerImage image) {
        this.image = image;
    }

    public AccountInfo getAccount() {
        return account;
    }

    public void setAccount(AccountInfo account) {
        this.account = account;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getProviderInstanceId() {
        return providerInstanceId;
    }

    public void setProviderInstanceId(String providerInstanceId) {
        this.providerInstanceId = providerInstanceId;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
