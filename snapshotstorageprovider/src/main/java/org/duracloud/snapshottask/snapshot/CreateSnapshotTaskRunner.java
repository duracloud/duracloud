/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import org.duracloud.common.constant.Constants;
import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.common.model.AclType;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.DateUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshottask.snapshot.dto.CreateSnapshotBridgeParameters;
import org.duracloud.snapshottask.snapshot.dto.CreateSnapshotTaskParameters;
import org.duracloud.snapshottask.snapshot.dto.CreateSnapshotTaskResult;
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

    private static final String TASK_NAME = "create-snapshot";

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
                                    String dcHost, String dcPort,
                                    String dcStoreId, String dcAccountName,
                                    String dcSnapshotUser, String bridgeAppHost,
                                    String bridgeAppPort, String bridgeAppUser,
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
        return TASK_NAME;
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
        CreateSnapshotTaskParameters taskParams = parseTaskParams(taskParameters);
        String spaceId = taskParams.getSpaceId();

        // Generate snapshot ID
        long now = System.currentTimeMillis();
        String snapshotId = dcAccountName + "-" + dcStoreId + "-" + spaceId +
                            "-"+ DateUtil.convertToStringPlain(now);

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

        log.info("Making SNAPSHOT call to URL {} with body {}",
                 snapshotURL, snapshotBody);

        // Make call to DPN bridge ingest app to kick off transfer
        RestHttpHelper restHelper =
            new RestHttpHelper(new Credential(bridgeAppUser, bridgeAppPass));
        try {
            restHelper.put(snapshotURL, snapshotBody, null);
        } catch(Exception e) {
            throw new TaskException("Exception encountered attempting to " +
                                    "initiate snapshot request. " +
                                    "Error reported: " + e.getMessage(), e);
        }

        log.info("SNAPSHOT with ID {} completed successfully ", snapshotId);

        return buildTaskResult(snapshotId);
    }

    /*
     * Give the snapshot user the necessary permissions to pull content from
     * the snapshot space.
     */
    protected void setSnapshotUserPermissions(String spaceId) {
        Map<String, AclType> spaceACLs = snapshotProvider.getSpaceACLs(spaceId);
        spaceACLs.put(StorageProvider.PROPERTIES_SPACE_ACL + dcSnapshotUser,
                      AclType.READ);
        snapshotProvider.setSpaceACLs(spaceId, spaceACLs);
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildSnapshotURL(String snapshotId) {
        String protocol = "http";
        if("443".equals(bridgeAppPort)) {
            protocol = "https";
        }
        return MessageFormat.format("{0}://{1}:{2}/bridge/snapshot/{3}",
                                    protocol, bridgeAppHost, bridgeAppPort,
                                    snapshotId);
    }

    /*
     * Creates the body of the request that will be sent to the bridge app
     */
    protected String buildSnapshotBody(CreateSnapshotTaskParameters taskParams) {
        CreateSnapshotBridgeParameters bridgeParams =
            new CreateSnapshotBridgeParameters();
        bridgeParams.setHost(dcHost);
        bridgeParams.setPort(dcPort);
        bridgeParams.setStoreId(dcStoreId);
        bridgeParams.setSpaceId(taskParams.getSpaceId());
        bridgeParams.setDescription(taskParams.getDescription());
        bridgeParams.setUserEmail(taskParams.getUserEmail());

        JaxbJsonSerializer<CreateSnapshotBridgeParameters> serializer =
            new JaxbJsonSerializer<>(CreateSnapshotBridgeParameters.class);
        try {
            return serializer.serialize(bridgeParams);
        } catch(IOException e) {
            throw new TaskException("Unable to parse task parameters due to: " +
                                    e.getMessage());
        }
    }

    /**
     * Parses spaceId and snapshot properties from task parameter string
     *
     * @param taskParameters - JSON formatted set of parameters
     */
    protected CreateSnapshotTaskParameters parseTaskParams(String taskParameters) {
        JaxbJsonSerializer<CreateSnapshotTaskParameters> serializer =
            new JaxbJsonSerializer<>(CreateSnapshotTaskParameters.class);
        try {
            return serializer.deserialize(taskParameters);
        } catch(IOException e) {
            throw new TaskException("Unable to parse task parameters due to: " +
                                    e.getMessage());
        }
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

    /**
     * Creates a serialized version of task results
     *
     * @param snapshotId
     * @return JSON formatted task result info
     */
    protected String buildTaskResult(String snapshotId) {
        CreateSnapshotTaskResult taskResult = new CreateSnapshotTaskResult();
        taskResult.setSnapshotId(snapshotId);

        // Parse spaceId and snapshot properties from taskParams
        JaxbJsonSerializer<CreateSnapshotTaskResult> serializer =
            new JaxbJsonSerializer<>(CreateSnapshotTaskResult.class);
        try {
            return serializer.serialize(taskResult);
        } catch(IOException e) {
            throw new TaskException("Unable to create task result due to: " +
                                    e.getMessage());
        }
    }

}
