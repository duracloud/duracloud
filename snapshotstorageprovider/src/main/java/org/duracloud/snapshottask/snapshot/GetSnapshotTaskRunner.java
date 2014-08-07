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
import org.duracloud.snapshot.dto.task.GetSnapshotTaskParameters;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Gets the status of an action to perform a snapshot
 *
 * @author Bill Branan
 *         Date: 7/23/14
 */
public class GetSnapshotTaskRunner implements TaskRunner {

    private static final String TASK_NAME = "get-snapshot";

    private Logger log =
        LoggerFactory.getLogger(GetSnapshotTaskRunner.class);

    private String bridgeAppHost;
    private String bridgeAppPort;
    private String bridgeAppUser;
    private String bridgeAppPass;

    public GetSnapshotTaskRunner(String bridgeAppHost,
                                       String bridgeAppPort,
                                       String bridgeAppUser,
                                       String bridgeAppPass) {
        this.bridgeAppHost = bridgeAppHost;
        this.bridgeAppPort = bridgeAppPort;
        this.bridgeAppUser = bridgeAppUser;
        this.bridgeAppPass = bridgeAppPass;
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        GetSnapshotTaskParameters params =
            GetSnapshotTaskParameters.deserialize(taskParameters);
        String snapshotId = params.getSnapshotId();

        RestHttpHelper restHelper =
            new RestHttpHelper(new Credential(bridgeAppUser, bridgeAppPass));
        return callBridge(restHelper, buildBridgeURL(snapshotId));
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildBridgeURL(String snapshotId) {
        String protocol = "443".equals(bridgeAppPort) ? "https" : "http";
        return MessageFormat.format("{0}://{1}:{2}/bridge/snapshot/{3}",
                                    protocol, bridgeAppHost, bridgeAppPort,
                                    snapshotId);
    }

    /*
     * Calls the bridge application to get snapshot listing
     */
    protected String callBridge(RestHttpHelper restHelper, String bridgeURL) {
        log.info("Making bridge call to get snapshot status. URL: {}", bridgeURL);

        try {
            RestHttpHelper.HttpResponse response = restHelper.get(bridgeURL);
            int statusCode = response.getStatusCode();
            if(statusCode != 200) {
                throw new RuntimeException("Unexpected response code: " +
                                           statusCode);
            }
            return response.getResponseBody();
        } catch(Exception e) {
            throw new TaskException("Exception encountered attempting to " +
                                    "get list of snapshots. " +
                                    "Error reported: " + e.getMessage(), e);
        }
    }

}
