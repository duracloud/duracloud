/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import org.apache.http.HttpHeaders;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.model.AclType;
import org.duracloud.common.retry.Retriable;
import org.duracloud.common.retry.Retrier;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.bridge.CreateRestoreBridgeParameters;
import org.duracloud.snapshot.dto.bridge.CreateRestoreBridgeResult;
import org.duracloud.snapshot.dto.task.RestoreSnapshotTaskParameters;
import org.duracloud.snapshot.dto.task.RestoreSnapshotTaskResult;
import org.duracloud.snapshot.id.SnapshotIdentifier;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Begins the process of restoring a snapshot by creating a landing space and
 * informing the snapshot bridge application that a restore action needs to be
 * performed.
 *
 * @author Bill Branan
 *         Date: 7/23/14
 */
public class RestoreSnapshotTaskRunner extends AbstractSnapshotTaskRunner {

    private Logger log =
        LoggerFactory.getLogger(RestoreSnapshotTaskRunner.class);

    private StorageProvider snapshotProvider;
    private SnapshotStorageProvider unwrappedSnapshotProvider;
    private String dcHost;
    private String dcPort;
    private String dcStoreId;
    private String dcSnapshotUser;

    public RestoreSnapshotTaskRunner(StorageProvider snapshotProvider,
                                     SnapshotStorageProvider unwrappedSnapshotProvider,
                                     String dcHost,
                                     String dcPort,
                                     String dcStoreId,
                                     String dcSnapshotUser,
                                     String bridgeAppHost,
                                     String bridgeAppPort,
                                     String bridgeAppUser,
                                     String bridgeAppPass) {
        super(bridgeAppHost, bridgeAppPort, bridgeAppUser, bridgeAppPass);
        this.snapshotProvider = snapshotProvider;
        this.unwrappedSnapshotProvider = unwrappedSnapshotProvider;
        this.dcHost = dcHost;
        this.dcPort = dcPort;
        this.dcStoreId = dcStoreId;
        this.dcSnapshotUser = dcSnapshotUser;
    }

    @Override
    public String getName() {
        return SnapshotConstants.RESTORE_SNAPSHOT_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        log.info("Performing RESTORE task with parameters, " +
                 "DuraCloud Host: {} DuraCloud Port: {} DuraCloud StoreID: {} " +
                 "DuraCloud Snapshot User: {} Bridge Host: {} Bridge Port: {} " +
                 "Bridge User: {}",
                  dcHost, dcPort, dcStoreId, dcSnapshotUser,
                  getBridgeAppHost(), getBridgeAppPort(), getBridgeAppUser());

        // Get input params
        RestoreSnapshotTaskParameters taskParams =
            RestoreSnapshotTaskParameters.deserialize(taskParameters);
        String snapshotId = taskParams.getSnapshotId();
        SnapshotIdentifier snapshotIdentifier;
        try {
            snapshotIdentifier = SnapshotIdentifier.parseSnapshotId(snapshotId);
        } catch(ParseException e) {
            throw new TaskException("Invalid Snapshot ID: " + snapshotId);
        }

        // Check to see if a restore of this snapshot is already available
        checkExistingRestore(snapshotIdentifier);

        // Create restore space
        String restoreSpaceId = snapshotIdentifier.getRestoreSpaceId();
        createSpace(restoreSpaceId);

        // Create URL for call to bridge app
        String bridgeURL = buildBridgeURL();

        // Create body for call to bridge app
        String bridgeBody = buildBridgeBody(restoreSpaceId,
                                            snapshotId,
                                            taskParams.getUserEmail());

        // Call to bridge to request restore
        String callResult =
            callBridge(createRestHelper(), bridgeURL, bridgeBody);
        CreateRestoreBridgeResult bridgeResult =
            CreateRestoreBridgeResult.deserialize(callResult);
        
        // Add restore ID to space properties
        addRestoreIdToSpaceProps(restoreSpaceId, bridgeResult.getRestoreId());

        // Set permissions on restore space
        setRestoreSpaceUserPermissions(restoreSpaceId);

        // Send response
        RestoreSnapshotTaskResult taskResult = new RestoreSnapshotTaskResult();
        taskResult.setSpaceId(restoreSpaceId);
        taskResult.setRestoreId(bridgeResult.getRestoreId());
        taskResult.setStatus(bridgeResult.getStatus());
        return taskResult.serialize();
    }


    /*
     * Determines if a restore of this snapshot is either already under way
     * or complete. This determination is based on the existance of a space
     * which matches the restore spaceId of the snapshot.
     *
     * Note: If the original spaceId used to take the snapshot was long
     * (and especially if multiple snapshots were taken on a single day), it
     * is possible for a name conflict to occur if multiple snapshots of the
     * same space are restored at the same time.
     */
    protected void checkExistingRestore(SnapshotIdentifier snapshotIdentifier) {
        Iterator<String> currentSpaces = snapshotProvider.getSpaces();
        while(currentSpaces.hasNext()) {
            String spaceId = currentSpaces.next();
            if(spaceId.equals(snapshotIdentifier.getRestoreSpaceId())) {
                // This snapshot has already been restored
                String error = "A request to restore snapshot with ID " +
                               snapshotIdentifier.getSnapshotId() +
                               " has been made previously. The snapshot is " +
                               "being restored to space: " + spaceId;
                throw new TaskException(error);
            }
        }
    }

    /*
     * Creates the space into which the restored snapshot content will be
     * placed during the restore process
     */
    protected String createSpace(final String spaceId) {
        try {
            Retrier retrier = new Retrier();
            return retrier.execute(new Retriable() {
                @Override
                public String retry() throws Exception {
                    // The actual method being executed
                    snapshotProvider.createSpace(spaceId);
                    return spaceId;
                }
            });
        } catch(Exception e) {
            throw new TaskException("Unable to initialize snapshot restore, " +
                                    "failed creating restore space due to: " +
                                    e.getMessage(), e);
        }
    }

    /*
     * Give the snapshot user the necessary permissions to add content
     */
    protected String setRestoreSpaceUserPermissions(final String spaceId) {
        try {
            Retrier retrier = new Retrier();
            return retrier.execute(new Retriable() {
                @Override
                public String retry() throws Exception {
                    // The actual method being executed
                    Map<String, AclType> spaceACLs = new HashMap<>();
                    spaceACLs.put(StorageProvider.PROPERTIES_SPACE_ACL +
                                  dcSnapshotUser, AclType.READ);
                    spaceACLs.put(StorageProvider.PROPERTIES_SPACE_ACL +
                                  dcSnapshotUser, AclType.WRITE);
                    snapshotProvider.setSpaceACLs(spaceId, spaceACLs);
                    return spaceId;
                }
            });
        } catch(Exception e) {
            throw new TaskException("Unable to initialize snapshot restore, " +
                                    "failed setting space permissions due to: " +
                                    e.getMessage(), e);
        }
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildBridgeURL() {
        return MessageFormat.format("{0}/restore", buildBridgeBaseURL());
    }

    /*
     * Creates the body of the request that will be sent to the bridge app
     */
    protected String buildBridgeBody(String spaceId,
                                     String snapshotId,
                                     String userEmail) {
        CreateRestoreBridgeParameters bridgeParams =
            new CreateRestoreBridgeParameters(dcHost, dcPort, dcStoreId,
                                              spaceId, snapshotId, userEmail);
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

    /*
     * Adds the restore ID as a property on the new restore space
     */
    protected void addRestoreIdToSpaceProps(String restoreSpaceId,
                                            String restoreId) {
        Map<String, String> restoreSpaceProps =
            snapshotProvider.getSpaceProperties(restoreSpaceId);
        restoreSpaceProps.put(Constants.RESTORE_ID_PROP,
                              restoreId);
        unwrappedSnapshotProvider.setNewSpaceProperties(restoreSpaceId,
                                                        restoreSpaceProps);
    }

}
