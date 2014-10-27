/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model;

import javax.persistence.MappedSuperclass;

/**
 * @author: Bill Branan
 * Date: 3/24/11
 */
@MappedSuperclass
public abstract class ProviderAccount extends BaseEntity {

    /**
     * The username necessary to connect to this provider's services. This may
     * have different names at each provider (e.g. at Amazon, this is the
     * Access Key ID)
     */
    protected String username;

    /**
     * The password necessary to connect to this provider's services. This may
     * have different names at each provider (e.g. at Amazon, this is the
     * Secret Access Key)
     */
    protected String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
