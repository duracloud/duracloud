/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHeaders;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.bridge.RequestRestoreBridgeParameters;
import org.duracloud.snapshot.dto.bridge.RequestRestoreBridgeResult;
import org.duracloud.snapshot.dto.task.RequestRestoreSnapshotParameters;
import org.duracloud.snapshot.dto.task.RequestRestoreSnapshotTaskResult;
import org.duracloud.snapshot.id.SnapshotIdentifier;
import org.duracloud.storage.error.TaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Sends a snapshot restore request to the duracloud admin for manual initiation. 
 *
 * @author Daniel Bernstein
 *         Date: 11/04/15
 */
public class RequestRestoreSnapshotTaskRunner extends AbstractSnapshotTaskRunner {

    private Logger log =
        LoggerFactory.getLogger(RequestRestoreSnapshotTaskRunner.class);

    private String dcHost;
    private String dcPort;
    private String dcStoreId;
    private String dcSnapshotUser;

    public RequestRestoreSnapshotTaskRunner(String dcHost,
                                     String dcPort,
                                     String dcStoreId,
                                     String dcSnapshotUser,
                                     String bridgeAppHost,
                                     String bridgeAppPort,
                                     String bridgeAppUser,
                                     String bridgeAppPass) {
        super(bridgeAppHost, bridgeAppPort, bridgeAppUser, bridgeAppPass);
        this.dcHost = dcHost;
        this.dcPort = dcPort;
        this.dcStoreId = dcStoreId;
        this.dcSnapshotUser = dcSnapshotUser;
    }

    @Override
    public String getName() {
        return SnapshotConstants.REQUEST_RESTORE_SNAPSHOT_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        log.info("Performing restore request task with parameters, " +
                 "DuraCloud Host: {} DuraCloud Port: {} DuraCloud StoreID: {} " +
                 "DuraCloud Snapshot User: {} Bridge Host: {} Bridge Port: {} " +
                 "Bridge User: {}",
                  dcHost, dcPort, dcStoreId, dcSnapshotUser,
                  getBridgeAppHost(), getBridgeAppPort(), getBridgeAppUser());

        // Get input params
        RequestRestoreSnapshotParameters taskParams =
            RequestRestoreSnapshotParameters.deserialize(taskParameters);
        String snapshotId = taskParams.getSnapshotId();
        SnapshotIdentifier snapshotIdentifier;
        try {
            snapshotIdentifier = SnapshotIdentifier.parseSnapshotId(snapshotId);
        } catch(ParseException e) {
            throw new TaskException("Invalid Snapshot ID: " + snapshotId);
        }

        // Create URL for call to bridge app
        String bridgeURL = buildBridgeURL();

        // Create body for call to bridge app
        String bridgeBody = buildBridgeBody(snapshotId,
                                            taskParams.getUserEmail());

        // Call to bridge to request restore
        String callResult =
            callBridge(createRestHelper(), bridgeURL, bridgeBody);
        RequestRestoreBridgeResult bridgeResult =
            RequestRestoreBridgeResult.deserialize(callResult);
        
        // Send response
        RequestRestoreSnapshotTaskResult taskResult = new RequestRestoreSnapshotTaskResult();
        taskResult.setDescription(bridgeResult.getDescription());
        return taskResult.serialize();
    }



    /*
     * Create URL to call bridge app
     */
    protected String buildBridgeURL() {
        return MessageFormat.format("{0}/restore/request", buildBridgeBaseURL());
    }

    /*
     * Creates the body of the request that will be sent to the bridge app
     */
    protected String buildBridgeBody(String snapshotId,
                                     String userEmail) {
        RequestRestoreBridgeParameters bridgeParams =
            new RequestRestoreBridgeParameters(dcHost, dcPort, dcStoreId, snapshotId, userEmail);
        return bridgeParams.serialize();
    }

    /*
     * Calls the bridge application to create a snapshot
     */
    protected String callBridge(RestHttpHelper restHelper,
                                String snapshotURL,
                                String snapshotBody) {
        log.info("Making RESTORE call to URL {} with body {}",
                 snapshotURL, snapshotBody);

        try {
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
            RestHttpHelper.HttpResponse response =
                restHelper.put(snapshotURL, snapshotBody, headers);
            int statusCode = response.getStatusCode();
            if(statusCode != 201) {
                throw new RuntimeException("Unexpected response code: " +
                                           statusCode);
            }
            return response.getResponseBody();
        } catch(Exception e) {
            throw new TaskException("Exception encountered attempting to " +
                                    "initiate snapshot request. " +
                                    "Error reported: " + e.getMessage(), e);
        }
    }

}
