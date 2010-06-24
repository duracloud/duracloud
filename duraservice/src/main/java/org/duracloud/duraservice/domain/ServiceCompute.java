/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.domain;

import org.duracloud.computeprovider.domain.ComputeProviderType;


/**
 * Stores the information necessary to perform actions
 * on a service compute instance
 *
 * @author Bill Branan
 */
public class ServiceCompute {

    private ComputeProviderType type;
    private String imageId;
    private String username;
    private String password;

    public ComputeProviderType getType() {
        return type;
    }

    public void setType(ComputeProviderType type) {
        this.type = type;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

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
