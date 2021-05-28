/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpHeaders;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.util.DateUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.bridge.CreateSnapshotBridgeParameters;
import org.duracloud.snapshot.dto.bridge.CreateSnapshotBridgeResult;
import org.duracloud.snapshot.dto.task.CreateSnapshotTaskParameters;
import org.duracloud.snapshot.id.SnapshotIdentifier;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.error.ServerConflictException;
import org.duracloud.storage.error.StorageStateException;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Begins the process of creating a snapshot by collecting the necessary
 * information and passing it down to the snapshot bridge application. Along
 * the way, the space provided is also set to read-only so that changes cannot
 * be made to the content.
 *
 * @author: Bill Branan
 * Date: 2/1/13
 */
public class CreateSnapshotTaskRunner extends SpaceModifyingSnapshotTaskRunner {

    private Logger log = LoggerFactory.getLogger(CreateSnapshotTaskRunner.class);

    private String dcAccountName;
    private String dcHost;
    private String dcPort;
    private String dcStoreId;
    private String bridgeMemberId;

    public CreateSnapshotTaskRunner(StorageProvider snapshotProvider,
                                    SnapshotStorageProvider unwrappedSnapshotProvider,
                                    String dcHost,
                                    String dcPort,
                                    String dcStoreId,
                                    String dcAccountName,
                                    String dcSnapshotUser,
                                    String bridgeAppHost,
                                    String bridgeAppPort,
                                    String bridgeAppUser,
                                    String bridgeAppPass,
                                    String bridgeMemberId) {
        super(snapshotProvider,
              unwrappedSnapshotProvider,
              dcSnapshotUser,
              bridgeAppHost,
              bridgeAppPort,
              bridgeAppUser,
              bridgeAppPass);
        this.dcHost = dcHost;
        this.dcPort = dcPort;
        this.dcStoreId = dcStoreId;
        this.dcAccountName = dcAccountName;
        this.bridgeMemberId = bridgeMemberId;
    }

    @Override
    public String getName() {
        return SnapshotConstants.CREATE_SNAPSHOT_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        log.info("Performing SNAPSHOT task with parameters, " +
                 "DuraCloud Host: {} DuraCloud Port: {} DuraCloud StoreID: {} " +
                 "Account Name: {} DuraCloud Snapshot User: {} Bridge Host: {} " +
                 "Bridge Port: {} Bridge User: {} Member Id: {}",
                 dcHost, dcPort, dcStoreId, dcAccountName, getSnapshotUser(),
                 getBridgeAppHost(), getBridgeAppPort(), getBridgeAppUser(), bridgeMemberId);

        // Get input params
        CreateSnapshotTaskParameters taskParams =
            CreateSnapshotTaskParameters.deserialize(taskParameters);
        String spaceId = taskParams.getSpaceId();

        //check if snapshot  properties file already exists
        //and if so throw StorageStateException
        String snapshotId = getSnapshotIdFromProperties(spaceId);
        if (snapshotId != null) {
            throw new StorageStateException(
                MessageFormat.format("A snapshot ({0}) + is already underway for this space ({1})",
                                     snapshotId, spaceId), null);
        }

        // Generate snapshot ID
        long now = System.currentTimeMillis();
        snapshotId = generateSnapshotId(spaceId, now);

        // Pull together all snapshot properties
        Map<String, String> snapshotProps = new HashMap<>();
        snapshotProps.put("duracloud-host", dcHost);
        snapshotProps.put("duracloud-space-id", spaceId);
        snapshotProps.put("duracloud-store-id", dcStoreId);
        snapshotProps.put(Constants.SNAPSHOT_ID_PROP, snapshotId);
        snapshotProps.put("snapshot-date", DateUtil.convertToStringVerbose(now));
        snapshotProps.put("owner-id", dcAccountName);
        snapshotProps.put("description", taskParams.getDescription());
        snapshotProps.put("user-email", taskParams.getUserEmail());
        snapshotProps.put("member-id", bridgeMemberId);

        // Store snapshot properties in the snapshot space. This both provides
        // access to the properties down stream and effectively sets the space
        // to a read-only state.
        String serializedProps = buildSnapshotProps(snapshotProps);
        storeSnapshotProps(spaceId, serializedProps);

        // Add snapshot ID to space properties
        addSnapshotIdToSpaceProps(spaceId, snapshotId);

        // Give snapshot user read permissions on space
        setSnapshotUserPermissions(spaceId);

        // Create URL for call to bridge app
        String snapshotURL = buildSnapshotURL(snapshotId);

        RestHttpHelper restHelper = createRestHelper();

        String callResult;
        try {
            // Create body for call to bridge app
            String snapshotBody = buildSnapshotBody(taskParams);

            // Make call to the bridge ingest app to kick off transfer
            callResult = callBridge(restHelper, snapshotURL, snapshotBody);
        } catch (Exception e) {
            // Bridge call did not complete successfully
            log.error("Call to Bridge to create snapshot {} failed due to: {}", snapshotId, e.getMessage());

            // Wait to give the Bridge time to initiate the snapshot
            wait(7);

            // Check if snapshot exists
            if (snapshotExists(restHelper, snapshotURL)) {
                log.info("Create snapshot call appeared to fail, but snapshot exists. " +
                         "Cleanup is being skipped to avoid removing files from an active snapshot.");
            } else {
                try {
                    // Clean up snapshot details
                    removeSnapshotProps(spaceId);
                    removeSnapshotIdFromSpaceProps(spaceId);
                } catch (Exception ex) {
                    log.error("Failed to fully clean up snapshot props for " +
                              spaceId + ": " + ex.getMessage(), ex);
                }
            }

            if (!(e instanceof TaskException)) {
                throw new TaskException(e.getMessage());
            } else {
                throw (TaskException) e;
            }
        }

        CreateSnapshotBridgeResult bridgeResult =
            CreateSnapshotBridgeResult.deserialize(callResult);
        log.info("SNAPSHOT created with ID {} and status {}",
                 bridgeResult.getSnapshotId(),
                 bridgeResult.getStatus());

        return callResult;
    }

    /*
     * Generates a snapshot Id based on a set of variables
     */
    protected String generateSnapshotId(String spaceId, long timestamp) {
        SnapshotIdentifier snapshotIdentifier =
            new SnapshotIdentifier(dcAccountName, dcStoreId, spaceId, timestamp);
        return snapshotIdentifier.getSnapshotId();
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildSnapshotURL(String snapshotId) {
        return MessageFormat.format("{0}/snapshot/{1}",
                                    buildBridgeBaseURL(),
                                    snapshotId);
    }

    /*
     * Creates the body of the request that will be sent to the bridge app
     */
    protected String buildSnapshotBody(CreateSnapshotTaskParameters taskParams) {
        CreateSnapshotBridgeParameters bridgeParams =
            new CreateSnapshotBridgeParameters(dcHost, dcPort, dcStoreId,
                                               taskParams.getSpaceId(),
                                               taskParams.getDescription(),
                                               taskParams.getUserEmail(),
                                               bridgeMemberId);
        return bridgeParams.serialize();
    }

    /**
     * Constructs the contents of a properties file given a set of
     * key/value pairs
     *
     * @param props snapshot properties
     * @return Properties-file formatted key/value pairs
     */
    protected String buildSnapshotProps(Map<String, String> props) {
        Properties snapshotProperties = new Properties();
        for (String key : props.keySet()) {
            snapshotProperties.setProperty(key, props.get(key));
        }

        StringWriter writer = new StringWriter();
        try {
            snapshotProperties.store(writer, null);
        } catch (IOException e) {
            throw new TaskException("Could not write snapshot properties: " +
                                    e.getMessage(), e);
        }
        writer.flush();
        return writer.toString();
    }

    /*
     * Calls the bridge application to create a snapshot
     */
    protected String callBridge(RestHttpHelper restHelper,
                                String snapshotURL,
                                String snapshotBody) throws Exception {
        log.info("Making Create SNAPSHOT call to URL {} with body {}", snapshotURL, snapshotBody);

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
        RestHttpHelper.HttpResponse response = restHelper.put(snapshotURL, snapshotBody, headers);
        int statusCode = response.getStatusCode();
        if (statusCode != 200 && statusCode != 201) {
            String responseStr = response.getResponseBody();
            log.warn("Create SNAPSHOT call returned an unexpected result, code: {}, body: {}",
                      statusCode, responseStr);

            try {
                String m = getMessageValue(responseStr);
                if (m != null) {
                    responseStr = m;
                }
            } catch (IOException ex) {
                log.warn(ex.getMessage(), ex);
            }

            if (statusCode == 409) {
                throw new ServerConflictException(responseStr);
            } else {
                throw new RuntimeException(responseStr + " (" + statusCode + ")");
            }

        }
        return response.getResponseBody();
    }

    /**
     * Attempts to retrieve details about the snapshot, primarily to determine if it was created properly
     *
     * @return true if 200 response, false otherwise
     */
    protected boolean snapshotExists(RestHttpHelper restHelper,
                                     String snapshotURL) {
        log.info("Making Get SNAPSHOT call to URL {}", snapshotURL);

        try {
            RestHttpHelper.HttpResponse response = restHelper.get(snapshotURL);

            int statusCode = response.getStatusCode();
            if (statusCode != 200) {
                log.info("Get SNAPSHOT call returned a non-200 result code: {}", statusCode);
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}