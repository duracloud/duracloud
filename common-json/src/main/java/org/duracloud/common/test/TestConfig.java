/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.test;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.duracloud.common.model.SimpleCredential;

/**
 * Test configuration information. To be used for integration tests which
 * need information about external systems in order to function.
 *
 * @author Bill Branan
 * Date: 7/29/13
 */
@XmlRootElement
public class TestConfig {

    public TestConfig() {
        providerCredentials = new ArrayList<>();
    }

    @XmlValue
    private List<StorageProviderCredential> providerCredentials;

    @XmlValue
    private String queueName;

    @XmlValue
    private TestEndPoint testEndPoint = new TestEndPoint();

    @XmlValue
    private SimpleCredential userCredential = new SimpleCredential("user", "upw");

    @XmlValue
    private SimpleCredential adminCredential = new SimpleCredential("admin", "apw");

    @XmlValue
    private SimpleCredential rootCredential = new SimpleCredential("root", "rpw");

    @XmlValue
    private String swiftEndpoint;

    @XmlValue
    private String swiftSignerType;

    public TestEndPoint getTestEndPoint() {
        return this.testEndPoint;
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

    public String getSwiftEndpoint() {
        return swiftEndpoint;
    }

    public void setSwiftEndpoint(String endpoint) {
        this.swiftEndpoint = endpoint;
    }

    public String getSwiftSignerType() {
        return swiftSignerType;
    }

    public void setSwiftSignerType(String signer) {
        this.swiftSignerType = signer;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public SimpleCredential getUserCredential() {
        return userCredential;
    }

    public void setUserCredential(SimpleCredential userCredential) {
        this.userCredential = userCredential;
    }

    public SimpleCredential getAdminCredential() {
        return adminCredential;
    }

    public void setAdminCredential(SimpleCredential adminCredential) {
        this.adminCredential = adminCredential;
    }

    public SimpleCredential getRootCredential() {
        return rootCredential;
    }

    public void setRootCredential(SimpleCredential rootCredential) {
        this.rootCredential = rootCredential;
    }

    public void setTestEndPoint(TestEndPoint testEndPoint) {
        this.testEndPoint = testEndPoint;
    }

}
