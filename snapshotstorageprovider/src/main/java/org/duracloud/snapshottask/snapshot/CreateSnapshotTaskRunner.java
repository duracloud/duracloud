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
import org.duracloud.common.model.Credential;
import org.duracloud.common.retry.Retriable;
import org.duracloud.common.retry.Retrier;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.DateUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.bridge.CreateSnapshotBridgeParameters;
import org.duracloud.snapshot.dto.bridge.CreateSnapshotBridgeResult;
import org.duracloud.snapshot.dto.task.CreateSnapshotTaskParameters;
import org.duracloud.snapshot.id.SnapshotIdentifier;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Begins the process of creating a snapshot by collecting the necessary
 * information and passing it down to the snapshot bridge application. Along
 * the way, the space provided is also set to read-only so that changes cannot
 * be made to the content.
 *
 * @author: Bill Branan
 *          Date: 2/1/13
 */
public class CreateSnapshotTaskRunner implements TaskRunner {

    private Logger log = LoggerFactory.getLogger(CreateSnapshotTaskRunner.class);

    private StorageProvider snapshotProvider;
    private String dcAccountName;
    private String dcHost;
    private String dcPort;
    private String dcStoreId;
    private String dcSnapshotUser;
    private String bridgeAppHost;
    private String bridgeAppPort;
    private String bridgeAppUser;
    private String bridgeAppPass;

    public CreateSnapshotTaskRunner(StorageProvider snapshotProvider,
                                    String dcHost,
                                    String dcPort,
                                    String dcStoreId,
                                    String dcAccountName,
                                    String dcSnapshotUser,
                                    String bridgeAppHost,
                                    String bridgeAppPort,
                                    String bridgeAppUser,
                                    String bridgeAppPass) {
        this.snapshotProvider = snapshotProvider;
        this.dcHost = dcHost;
        this.dcPort = dcPort;
        this.dcStoreId = dcStoreId;
        this.dcAccountName = dcAccountName;
        this.dcSnapshotUser = dcSnapshotUser;
        this.bridgeAppHost = bridgeAppHost;
        this.bridgeAppPort = bridgeAppPort;
        this.bridgeAppUser = bridgeAppUser;
        this.bridgeAppPass = bridgeAppPass;
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
                 "Bridge Port: {} Bridge User: {}",
                 new Object[] {dcHost, dcPort, dcStoreId, dcAccountName,
                               dcSnapshotUser, bridgeAppHost, bridgeAppPort,
                               bridgeAppUser});

        // Get input params
        CreateSnapshotTaskParameters taskParams =
            CreateSnapshotTaskParameters.deserialize(taskParameters);
        String spaceId = taskParams.getSpaceId();

        // Generate snapshot ID
        long now = System.currentTimeMillis();
        SnapshotIdentifier snapshotIdentifier =
            new SnapshotIdentifier(dcAccountName, dcStoreId, spaceId, now);
        String snapshotId = snapshotIdentifier.getSnapshotId();

        // Pull together all snapshot properties
        Map<String, String> snapshotProps = new HashMap<>();
        snapshotProps.put("duracloud-host", dcHost);
        snapshotProps.put("duracloud-space-id", spaceId);
        snapshotProps.put("duracloud-store-id", dcStoreId);
        snapshotProps.put("snapshot-id", snapshotId);
        snapshotProps.put("snapshot-date", DateUtil.convertToStringVerbose(now));
        snapshotProps.put("owner-id", dcAccountName);
        snapshotProps.put("description", taskParams.getDescription());
        snapshotProps.put("user-email", taskParams.getUserEmail());

        // Store snapshot properties in the snapshot space. This both provides
        // access to the properties down stream and effectively sets the space
        // to a read-only state.
        String serializedProps = buildSnapshotProps(snapshotProps);
        storeSnapshotProps(spaceId, serializedProps);

        // Give snapshot user read permissions on space
        setSnapshotUserPermissions(spaceId);

        // Create URL for call to bridge app
        String snapshotURL = buildSnapshotURL(snapshotId);

        // Create body for call to bridge app
        String snapshotBody = buildSnapshotBody(taskParams);

        // Make call to DPN bridge ingest app to kick off transfer
        RestHttpHelper restHelper =
            new RestHttpHelper(new Credential(bridgeAppUser, bridgeAppPass));
        String callResult = callBridge(restHelper, snapshotURL, snapshotBody);

        CreateSnapshotBridgeResult bridgeResult =
            CreateSnapshotBridgeResult.deserialize(callResult);
        log.info("SNAPSHOT created with ID {} and status {}",
                 bridgeResult.getSnapshotId(),
                 bridgeResult.getStatus());

        return callResult;
    }

    /*
     * Give the snapshot user the necessary permissions to pull content from
     * the snapshot space.
     */
    protected String setSnapshotUserPermissions(final String spaceId) {
        try {
            Retrier retrier = new Retrier();
            return retrier.execute(new Retriable() {
                @Override
                public String retry() throws Exception {
                    // The actual method being executed
                    Map<String, AclType> spaceACLs =
                        snapshotProvider.getSpaceACLs(spaceId);
                    spaceACLs.put(StorageProvider.PROPERTIES_SPACE_ACL +
                                  dcSnapshotUser, AclType.READ);
                    snapshotProvider.setSpaceACLs(spaceId, spaceACLs);
                    return spaceId;
                }
            });
        } catch(Exception e) {
            throw new TaskException("Unable to create snapshot, failed" +
                                    "setting space permissions due to: " +
                                    e.getMessage(), e);
        }
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildSnapshotURL(String snapshotId) {
        String protocol = "443".equals(bridgeAppPort) ? "https" : "http";
        return MessageFormat.format("{0}://{1}:{2}/bridge/snapshot/{3}",
                                    protocol, bridgeAppHost, bridgeAppPort,
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
                                               taskParams.getUserEmail());
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
        for(String key : props.keySet()) {
            snapshotProperties.setProperty(key, props.get(key));
        }

        StringWriter writer = new StringWriter();
        try {
            snapshotProperties.store(writer, null);
        } catch(IOException e) {
            throw new TaskException("Could not write snapshot properties: " +
                                    e.getMessage(), e);
        }
        writer.flush();
        return writer.toString();
    }

    /**
     * Stores a set of snapshot properties in the given space as a properties
     * file.
     *
     * @param spaceId the space in which the properties file should be stored
     * @param serializedProps properties in serialized format
     */
    protected void storeSnapshotProps(String spaceId, String serializedProps) {
        InputStream propsStream;
        try {
            propsStream = IOUtil.writeStringToStream(serializedProps);
        } catch(IOException e) {
            throw new TaskException("Unable to build stream from serialized " +
                                    "snapshot properties due to: " +
                                    e.getMessage());
        }
        ChecksumUtil checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String propsChecksum = checksumUtil.generateChecksum(serializedProps);

        snapshotProvider.addContent(spaceId,
                                    Constants.SNAPSHOT_ID,
                                    "text/x-java-properties",
                                    null,
                                    serializedProps.length(),
                                    propsChecksum,
                                    propsStream);
    }

    /*
     * Calls the bridge application to create a snapshot
     */
    protected String callBridge(RestHttpHelper restHelper,
                                String snapshotURL,
                                String snapshotBody) {
        log.info("Making SNAPSHOT call to URL {} with body {}",
                 snapshotURL, snapshotBody);

        try {
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
            RestHttpHelper.HttpResponse response =
                restHelper.put(snapshotURL, snapshotBody, headers);
            int statusCode = response.getStatusCode();
            if(statusCode != 200 && statusCode != 201) {
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
