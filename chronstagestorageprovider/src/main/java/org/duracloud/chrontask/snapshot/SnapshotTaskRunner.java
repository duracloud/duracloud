/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chrontask.snapshot;

import org.duracloud.chronstorage.ChronStageStorageProvider;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.common.model.AclType;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.DateUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.TaskRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

/**
 * @author: Bill Branan
 *          Date: 2/1/13
 */
public class SnapshotTaskRunner implements TaskRunner {

    private static final String TASK_NAME = "snapshot";
    private static final String SNAPSHOT_USER = "chronopolis";

    private ChronStageStorageProvider chronProvider;

    public SnapshotTaskRunner(ChronStageStorageProvider chronProvider) {
        this.chronProvider = chronProvider;
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        // TODO: Get access to these values
        String dcHost = "";
        String ownerId = "";
        String storeId = "";
        String bridgeAppHost = "";
        String bridgeAppUser = "";
        String bridgeAppPass = "";

        // Get input params
        SnapshotTaskParameters taskParams = parseTaskParams(taskParameters);
        String spaceId = taskParams.getSpaceId();
        Map<String, String> snapshotProps = taskParams.getSnapshotProperties();

        // Generate snapshot ID
        String snapshotId = ownerId + "-" + storeId + "-" + spaceId +
                            "-"+ DateUtil.nowPlain();

        // Pull together all snapshot properties
        snapshotProps.put("duracloud-host", dcHost);
        snapshotProps.put("duracloud-space-id", spaceId);
        snapshotProps.put("duracloud-store-id", storeId);
        snapshotProps.put("snapshot-id", snapshotId);
        snapshotProps.put("owner-id", ownerId);

        // Store snapshot properties in the snapshot space. This both provides
        // access to the properties down stream and effectively sets the space
        // to a read-only state.
        String serializedProps = buildSnapshotProps(snapshotProps);
        storeSnapshotProps(spaceId, serializedProps);

        // Give snapshot user read permissions on space
        Map<String, AclType> spaceACLs = chronProvider.getSpaceACLs(spaceId);
        spaceACLs.put(SNAPSHOT_USER, AclType.READ);
        chronProvider.setSpaceACLs(spaceId, spaceACLs);

        // Create URL to call bridge app
        // format: host/storeId/spaceId/snapshotId
        String snapshotURL = "http://"+ bridgeAppHost + "/snapshot/" + dcHost +
                             "/" + storeId + "/" + spaceId + "/" + snapshotId;

        // Make call to DPN bridge ingest app to kick off transfer
        RestHttpHelper restHelper =
            new RestHttpHelper(new Credential(bridgeAppUser, bridgeAppPass));
        try {
            restHelper.post(snapshotURL, null, null);
        } catch(Exception e) {
            throw new TaskException("Exception encountered attempting to " +
                                    "initiate snapshot request. " +
                                    "Error reported: " + e.getMessage(), e);
        }

        return buildTaskResult(snapshotId);
    }

    /**
     * Parses spaceId and snapshot properties from task parameter string
     *
     * @param taskParameters - JSON formatted set of parameters
     */
    protected SnapshotTaskParameters parseTaskParams(String taskParameters) {
        JaxbJsonSerializer<SnapshotTaskParameters> serializer =
            new JaxbJsonSerializer<>(SnapshotTaskParameters.class);
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

        chronProvider.addContent(spaceId,
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
        SnapshotTaskResult taskResult = new SnapshotTaskResult();
        taskResult.setSnapshotId(snapshotId);

        // Parse spaceId and snapshot properties from taskParams
        JaxbJsonSerializer<SnapshotTaskResult> serializer =
            new JaxbJsonSerializer<>(SnapshotTaskResult.class);
        try {
            return serializer.serialize(taskResult);
        } catch(IOException e) {
            throw new TaskException("Unable to create task result due to: " +
                                    e.getMessage());
        }
    }

}
