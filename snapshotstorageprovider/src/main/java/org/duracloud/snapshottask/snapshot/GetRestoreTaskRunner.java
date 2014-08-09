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
import org.duracloud.snapshot.dto.task.GetRestoreTaskParameters;
import org.duracloud.storage.error.TaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Gets the status and details of a snapshot restore action.
 *
 * @author Bill Branan
 *         Date: 7/23/14
 */
public class GetRestoreTaskRunner extends AbstractSnapshotTaskRunner {

    private Logger log = LoggerFactory.getLogger(GetRestoreTaskRunner.class);

 
    public GetRestoreTaskRunner(String bridgeAppHost,
                                String bridgeAppPort,
                                String bridgeAppUser,
                                String bridgeAppPass) {
        super(bridgeAppHost, bridgeAppPort, bridgeAppUser, bridgeAppPass);
    }
    
    @Override
    public String getName() {
        return SnapshotConstants.GET_RESTORE_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        GetRestoreTaskParameters params =
            GetRestoreTaskParameters.deserialize(taskParameters);
        return callBridge(createRestHelper(), buildBridgeURL(params));
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildBridgeURL(GetRestoreTaskParameters params) {
        if(params.getRestoreId() != null){
            return MessageFormat.format("{0}/restore/{1}",
                                        buildBridgeBaseURL(),
                                        params.getRestoreId());
            
        }else{
            return MessageFormat.format("{0}/restore/by-snapshot/{1}",
                                        buildBridgeBaseURL(),
                                        params.getSnapshotId());
        }
    }

    /*
     * Calls the bridge application to get snapshot listing
     */
    protected String callBridge(RestHttpHelper restHelper, String bridgeURL) {
        log.info("Making bridge call to get restore status. URL: {}", bridgeURL);

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
                                    "get restore. " +
                                    "Error reported: " + e.getMessage(), e);
        }
    }

}
