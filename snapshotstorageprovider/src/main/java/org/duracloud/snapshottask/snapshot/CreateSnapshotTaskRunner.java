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
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.DateUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.bridge.CreateSnapshotBridgeParameters;
import org.duracloud.snapshot.dto.bridge.CreateSnapshotBridgeResult;
import org.duracloud.snapshot.dto.task.CreateSnapshotTaskParameters;
import org.duracloud.snapshot.error.SnapshotDataException;
import org.duracloud.snapshot.id.SnapshotIdentifier;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.StorageProvider;
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
public class CreateSnapshotTaskRunner extends AbstractSnapshotTaskRunner {

    private Logger log = LoggerFactory.getLogger(CreateSnapshotTaskRunner.class);

    private StorageProvider snapshotProvider;
    private SnapshotStorageProvider unwrappedSnapshotProvider;
    private String dcAccountName;
    private String dcHost;
    private String dcPort;
    private String dcStoreId;
    private String dcSnapshotUser;

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
                                    String bridgeAppPass) {
        super(bridgeAppHost, bridgeAppPort, bridgeAppUser, bridgeAppPass);
        this.snapshotProvider = snapshotProvider;
        this.unwrappedSnapshotProvider = unwrappedSnapshotProvider;
        this.dcHost = dcHost;
        this.dcPort = dcPort;
        this.dcStoreId = dcStoreId;
        this.dcAccountName = dcAccountName;
        this.dcSnapshotUser = dcSnapshotUser;
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
                 dcHost, dcPort, dcStoreId, dcAccountName, dcSnapshotUser,
                 getBridgeAppHost(), getBridgeAppPort(), getBridgeAppUser());

        // Get input params
        CreateSnapshotTaskParameters taskParams =
            CreateSnapshotTaskParameters.deserialize(taskParameters);
        String spaceId = taskParams.getSpaceId();

        // Generate snapshot ID
        long now = System.currentTimeMillis();
        String snapshotId = generateSnapshotId(spaceId, now);

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

        // Add snapshot ID to space properties
        addSnapshotIdToSpaceProps(spaceId, snapshotId);

        // Give snapshot user read permissions on space
        setSnapshotUserPermissions(spaceId);

        // Create URL for call to bridge app
        String snapshotURL = buildSnapshotURL(snapshotId);

        String callResult;
        try {
            // Create body for call to bridge app
            String snapshotBody = buildSnapshotBody(taskParams);

            // Make call to DPN bridge ingest app to kick off transfer
            callResult =
                callBridge(createRestHelper(), snapshotURL, snapshotBody);
        } catch(TaskException | SnapshotDataException e) {
            // Bridge call did not complete successfully, clean up!
            try{
                removeSnapshotProps(spaceId);
                removeSnapshotIdFromSpaceProps(spaceId);
            }catch(Exception ex){
                log.error("Failed to fully clean up snapshot props for " +
                          spaceId + ": " + ex.getMessage(), ex);
            }
            String msg = MessageFormat.format("Call to create snapshot failed, " +
                "snapshot properties have been removed from space {0}. " +
                "Error message: {1}", spaceId, e.getMessage());
            throw new TaskException(msg, e);
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
     * Adds a snapshot ID property to the space
     */
    protected void addSnapshotIdToSpaceProps(String spaceId, String snapshotId) {
        Map<String, String> spaceProps =
            snapshotProvider.getSpaceProperties(spaceId);
        spaceProps.put(Constants.SNAPSHOT_ID_PROP, snapshotId);
        unwrappedSnapshotProvider.setNewSpaceProperties(spaceId, spaceProps);
    }

    /*
     * Removes the snapshot ID property from a space
     */
    protected void removeSnapshotIdFromSpaceProps(String spaceId) {
        log.debug("Removing " + Constants.SNAPSHOT_ID_PROP +
                  " property from space " + spaceId);
        Map<String, String> spaceProps =
            snapshotProvider.getSpaceProperties(spaceId);
        if(spaceProps.remove(Constants.SNAPSHOT_ID_PROP) != null){
            unwrappedSnapshotProvider.setNewSpaceProperties(spaceId, spaceProps);
            log.info("Removed " + Constants.SNAPSHOT_ID_PROP +
                     " from  space properties for space " + spaceId);
        }else{
            log.debug("Property " + Constants.SNAPSHOT_ID_PROP +
                      " does not exist in space properties for " + spaceId +
                      ". No need to update space properties.");
        }
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
                                    Constants.SNAPSHOT_PROPS_FILENAME,
                                    "text/x-java-properties",
                                    null,
                                    serializedProps.length(),
                                    propsChecksum,
                                    propsStream);
    }

    protected void removeSnapshotProps(String spaceId) {
        snapshotProvider.deleteContent(spaceId, Constants.SNAPSHOT_PROPS_FILENAME);
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
