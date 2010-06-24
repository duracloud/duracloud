/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.computeprovider.mgmt.mock;

import java.net.URL;

import org.duracloud.common.model.Credential;
import org.duracloud.computeprovider.mgmt.InstanceDescription;
import org.duracloud.computeprovider.mgmt.ComputeProvider;

public class LocalComputeProviderImpl
        implements ComputeProvider {

    private final String instanceId = "mockInstanceId";

    private final String url = "http://localhost:8080/instancewebapp";

    public InstanceDescription describeRunningInstance(Credential credential,
                                                       String instanceId,
                                                       String xmlProps) {
        return new MockInstanceDescription();
    }

    public URL getWebappURL(Credential credential,
                            String instanceId,
                            String xmlProps) throws Exception {
        if (!isInstanceRunning(credential, instanceId, xmlProps)) {
            throw new Exception("Mock web app is not running: no url!");
        }
        return new URL(url);
    }

    public boolean isInstanceBooting(Credential credential,
                                     String instanceId,
                                     String xmlProps) throws Exception {
        return false;
    }

    public boolean isInstanceRunning(Credential credential,
                                     String instanceId,
                                     String xmlProps) throws Exception {
        return this.instanceId.equals(instanceId);
    }

    public boolean isWebappRunning(Credential credential,
                                   String instanceId,
                                   String xmlProps) throws Exception {
        return this.instanceId.equals(instanceId);
    }

    public String start(Credential cred, String xmlProps) throws Exception {
        return instanceId;
    }

    public void stop(Credential credential, String instanceId, String xmlProps)
            throws Exception {
    }

}
