/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.test;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.ArrayList;
import java.util.List;

/**
 * Test configuration information. To be used for integration tests which
 * need information about external systems in order to function.
 *
 * @author Bill Branan
 *         Date: 7/29/13
 */
@XmlRootElement
public class TestConfig {

    @XmlValue
    private List<StorageProviderCredential> providerCredentials;

    @XmlValue
    private String queueName;

    public TestConfig() {
        providerCredentials = new ArrayList<>();
    }

    public void addProviderCredential(StorageProviderCredential cred) {
        providerCredentials.add(cred);
    }

    public List<StorageProviderCredential> getProviderCredentials() {
        return providerCredentials;
    }

    public void setProviderCredentials(
        List<StorageProviderCredential> providerCredentials) {
        this.providerCredentials = providerCredentials;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

}
