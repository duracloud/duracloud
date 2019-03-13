/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.test;

import javax.xml.bind.annotation.XmlValue;

import org.duracloud.common.model.SimpleCredential;

/**
 * Simple set of information that is needed to connect to a storage provider.
 *
 * @author Bill Branan
 * Date: 7/29/13
 */
public class StorageProviderCredential {

    public static enum ProviderType {
        AMAZON_S3, SWIFT_S3, AMAZON_GLACIER
    }

    @XmlValue
    private ProviderType type;

    @XmlValue
    private SimpleCredential credential;

    public StorageProviderCredential() {
    }

    public StorageProviderCredential(ProviderType type,
                                     SimpleCredential credential) {
        this.type = type;
        this.credential = credential;
    }

    public ProviderType getType() {
        return type;
    }

    public void setType(ProviderType type) {
        this.type = type;
    }

    public SimpleCredential getCredential() {
        return credential;
    }

    public void setCredential(SimpleCredential credential) {
        this.credential = credential;
    }

}
