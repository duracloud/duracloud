/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.task.GetSnapshotTaskParameters;
import org.duracloud.storage.error.TaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Gets the status and details of a snapshot action
 *
 * @author Bill Branan
 *         Date: 7/23/14
 */
public class GetSnapshotTaskRunner extends AbstractSnapshotTaskRunner {

    private Logger log =
        LoggerFactory.getLogger(GetSnapshotTaskRunner.class);


    public GetSnapshotTaskRunner(String bridgeAppHost,
                                 String bridgeAppPort,
                                 String bridgeAppUser,
                                 String bridgeAppPass) {
        super(bridgeAppHost, bridgeAppPort, bridgeAppUser, bridgeAppPass);
    }

    @Override
    public String getName() {
        return SnapshotConstants.GET_SNAPSHOT_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        GetSnapshotTaskParameters params =
            GetSnapshotTaskParameters.deserialize(taskParameters);
        String snapshotId = params.getSnapshotId();

        return callBridge(createRestHelper(), buildBridgeURL(snapshotId));
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildBridgeURL(String snapshotId) {
        return MessageFormat.format("{0}/snapshot/{1}",
                                    buildBridgeBaseURL(),
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
                                    "get  snapshot. " +
                                    "Error reported: " + e.getMessage(), e);
        }
    }

}
