/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import java.text.MessageFormat;

import org.apache.http.HttpStatus;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.task.RestartSnapshotTaskParameters;
import org.duracloud.storage.error.TaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Restarts a snapshot that failed to successfully transfer content
 * from DuraCloud to the bridge.
 *
 * @author Daniel Bernstein 
 *         Date: 08/10/15
 */
public class RestartSnapshotTaskRunner extends AbstractSnapshotTaskRunner {

    private Logger log = LoggerFactory.getLogger(RestartSnapshotTaskRunner.class);

    private String dcHost;

    public RestartSnapshotTaskRunner(String bridgeAppHost,
                                  String bridgeAppPort,
                                  String bridgeAppUser,
                                  String bridgeAppPass) {
        super(bridgeAppHost, bridgeAppPort, bridgeAppUser, bridgeAppPass);
    }

    @Override
    public String getName() {
        return SnapshotConstants.RESTART_SNAPSHOT_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        RestartSnapshotTaskParameters params =
            RestartSnapshotTaskParameters.deserialize(taskParameters);
        String snapshotId = params.getSnapshotId();

        return callBridge(createRestHelper(), buildBridgeURL(snapshotId));
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildBridgeURL(String snapshotId) {
        return MessageFormat.format("{0}/snapshot/{1}/restart",
                                    buildBridgeBaseURL(),
                                    snapshotId);
    }

    /*
     * Calls the bridge application to get snapshot listing
     */
    protected String callBridge(RestHttpHelper restHelper, String bridgeURL) {
        log.info("Making bridge call to restart snapshot. URL: {}", bridgeURL);

        try {
            RestHttpHelper.HttpResponse response = restHelper.post(bridgeURL, null, null);
            int statusCode = response.getStatusCode();
            if(statusCode != HttpStatus.SC_ACCEPTED) {
                throw new RuntimeException("Unexpected response code: " +
                                           statusCode);
            }
            return response.getResponseBody();
        } catch(Exception e) {
            throw new TaskException("Exception encountered attempting to " +
                                    "restart snapshot. " +
                                    "Error reported: " + e.getMessage(), e);
        }
    }

}
