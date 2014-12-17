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

/**
 * @author Erik Paulsson
 *         Date: 7/10/13
 */
@Entity
public class ServerImage extends BaseEntity {

    /**
     * The username of the DuraCloud root user which can be used to perform
     * actions on DuraCloud instances created from this image
     */
    public static final String DC_ROOT_USERNAME = "root";

    /**
     * The ID given to this image by the hosting provider. This will likely
     * have a different name at each provider (e.g. at Amazon this is the
     * AMI ID, the Amazon Machine Image Identifier).
     */
    private String providerImageId;

    /**
     * The version of the DuraCloud software loaded on this image.
     */
    private String version;

    /**
     * Description of the image.
     */
    private String description;

    /**
     * The password of the DuraCloud root user which can be used to perform
     * actions on DuraCloud instances created from this image
     */
    private String dcRootPassword;

    /**
     * If true, indicates that this is the latest version in the database. This
     * value should be true on one and only one row in the Image table.
     */
    private boolean latest;

    public String getProviderImageId() {
        return providerImageId;
    }

    public void setProviderImageId(String providerImageId) {
        this.providerImageId = providerImageId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDcRootPassword() {
        return dcRootPassword;
    }

    public void setDcRootPassword(String dcRootPassword) {
        this.dcRootPassword = dcRootPassword;
    }

    public boolean isLatest() {
        return latest;
    }

    public void setLatest(boolean latest) {
        this.latest = latest;
    }
}
