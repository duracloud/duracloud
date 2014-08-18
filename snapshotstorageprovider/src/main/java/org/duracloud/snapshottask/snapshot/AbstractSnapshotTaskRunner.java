/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import org.duracloud.common.model.Credential;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.storage.provider.TaskRunner;

import java.text.MessageFormat;

public abstract class AbstractSnapshotTaskRunner implements TaskRunner {

    private String bridgeAppHost;
    private String bridgeAppPort;
    private String bridgeAppUser;
    private String bridgeAppPass;

    public AbstractSnapshotTaskRunner(
        String bridgeAppHost, String bridgeAppPort, String bridgeAppUser,
        String bridgeAppPass) {
        this.bridgeAppHost = bridgeAppHost;
        this.bridgeAppPort = bridgeAppPort;
        this.bridgeAppUser = bridgeAppUser;
        this.bridgeAppPass = bridgeAppPass;
    }

    protected String getBridgeAppHost() {
        return bridgeAppHost;
    }

    protected String getBridgeAppPort() {
        return bridgeAppPort;
    }

    protected String getBridgeAppUser() {
        return bridgeAppUser;
    }

    protected String getBridgeAppPass() {
        return bridgeAppPass;
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildBridgeBaseURL() {
        String protocol = "443".equals(bridgeAppPort) ? "https" : "http";
        return MessageFormat.format("{0}://{1}:{2}/bridge",
                                    protocol,
                                    bridgeAppHost,
                                    bridgeAppPort);
    }
    
    protected RestHttpHelper createRestHelper(){
        RestHttpHelper restHelper = 
            new RestHttpHelper(new Credential(bridgeAppUser, bridgeAppPass));
        return restHelper;
    }
}
