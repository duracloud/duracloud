/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.domain;

import org.duracloud.common.model.Credential;

/**
 * Contains the information necessary to connect to a DuraCloud
 * store which houses service packages
 *
 * @author Bill Branan
 */
public class ServiceStore extends Store {

    private String spaceId;
    private String username;
    private String password;

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Credential getCredential() {
        return new Credential(username, password);
    }
}
