/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.task;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.common.queue.task.Task;
import org.duracloud.common.queue.task.TypedTask;

/**
 * A Task which will be used to capture an action taken on a
 * DuraCloud content item.
 *
 * @author Bill Branan
 * Date: 3/14/14
 */
public class AuditTask extends TypedTask {

    public static final String ACTION_PROP = "action";
    public static final String USER_ID_PROP = "user-id";
    public static final String DATE_TIME_PROP = "date-time";
    public static final String CONTENT_CHECKSUM_PROP = "content-checksum";
    public static final String CONTENT_MIMETYPE_PROP = "content-mimetype";
    public static final String CONTENT_SIZE_PROP = "content-size";
    public static final String CONTENT_RANGE_PROP = "content-range";
    public static final String CONTENT_PROPERTIES_PROP = "content-properties";
    public static final String SPACE_ACLS_PROP = "space-acls";
    public static final String STORE_TYPE_PROP = "store-type";
    public static final String SOURCE_SPACE_ID_PROP = "source-space-id";
    public static final String SOURCE_CONTENT_ID_PROP = "source-content-id";

    public enum ActionType {
        // Write actions
        CREATE_SPACE, DELETE_SPACE, SET_SPACE_ACLS, ADD_CONTENT,
        COPY_CONTENT, DELETE_CONTENT, SET_CONTENT_PROPERTIES,
        // Read actions
        GET_SPACES, GET_SPACE_CONTENTS, GET_SPACE_CONTENTS_CHUNKED,
        GET_SPACE_PROPERTIES, GET_SPACE_ACLS, GET_CONTENT,
        GET_CONTENT_PROPERTIES
    }

    private String action;
    private String userId;
    private String dateTime;
    private String contentChecksum;
    private String contentMimetype;
    private String contentSize;
    private String contentRange;
    private Map<String, String> contentProperties;
    private String spaceACLs;
    private String storeType;
    private String sourceSpaceId;
    private String sourceContentId;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        // Throws IllegalArgumentException if action is not an ActionType value
        ActionType.valueOf(action);
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getContentChecksum() {
        return contentChecksum;
    }

    public void setContentChecksum(String contentChecksum) {
        this.contentChecksum = contentChecksum;
    }

    public String getContentMimetype() {
        return contentMimetype;
    }

    public void setContentMimetype(String contentMimetype) {
        this.contentMimetype = contentMimetype;
    }

    public String getContentSize() {
        return contentSize;
    }

    public void setContentSize(String contentSize) {
        this.contentSize = contentSize;
    }

    public String getContentRange() {
        return contentRange;
    }

    public void setContentRange(String contentRange) {
        this.contentRange = contentRange;
    }

    public Map<String, String> getContentProperties() {
        return contentProperties;
    }

    public void setContentProperties(Map<String, String> contentProperties) {
        this.contentProperties = contentProperties;
    }

    public String getSpaceACLs() {
        return spaceACLs;
    }

    public void setSpaceACLs(String spaceACLs) {
        this.spaceACLs = spaceACLs;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getSourceSpaceId() {
        return sourceSpaceId;
    }

    public void setSourceSpaceId(String sourceSpaceId) {
        this.sourceSpaceId = sourceSpaceId;
    }

    public String getSourceContentId() {
        return sourceContentId;
    }

    public void setSourceContentId(String sourceContentId) {
        this.sourceContentId = sourceContentId;
    }

    @Override
    public void readTask(Task task) {
        super.readTask(task);

        Map<String, String> props = task.getProperties();
        setAction(props.get(ACTION_PROP));
        setUserId(props.get(USER_ID_PROP));
        setDateTime(props.get(DATE_TIME_PROP));
        setContentChecksum(props.get(CONTENT_CHECKSUM_PROP));
        setContentMimetype(props.get(CONTENT_MIMETYPE_PROP));
        setContentSize(props.get(CONTENT_SIZE_PROP));
        Map<String, String> contentProps =
            deserializeContentProperties(props.get(CONTENT_PROPERTIES_PROP));
        setContentProperties(contentProps);
        setSpaceACLs(props.get(SPACE_ACLS_PROP));
        setStoreType(props.get(STORE_TYPE_PROP));
        setSourceSpaceId(props.get(SOURCE_SPACE_ID_PROP));
        setSourceContentId(props.get(SOURCE_CONTENT_ID_PROP));
    }

    @Override
    public Task writeTask() {
        Task task = super.writeTask();
        task.setType(Task.Type.AUDIT);
        addProperty(task, ACTION_PROP, getAction());
        addProperty(task, USER_ID_PROP, getUserId());
        addProperty(task, DATE_TIME_PROP, getDateTime());
        addProperty(task, CONTENT_CHECKSUM_PROP, getContentChecksum());
        addProperty(task, CONTENT_MIMETYPE_PROP, getContentMimetype());
        addProperty(task, CONTENT_SIZE_PROP, getContentSize());
        addProperty(task, CONTENT_RANGE_PROP, getContentRange());
        String contentProps = serializeContentProperties(getContentProperties());
        addProperty(task, CONTENT_PROPERTIES_PROP, contentProps);
        addProperty(task, SPACE_ACLS_PROP, this.spaceACLs);
        addProperty(task, STORE_TYPE_PROP, getStoreType());
        addProperty(task, SOURCE_SPACE_ID_PROP, getSourceSpaceId());
        addProperty(task, SOURCE_CONTENT_ID_PROP, getSourceContentId());
        return task;
    }

    protected static Map<String, String> deserializeContentProperties(String json) {
        if (StringUtils.isNotBlank(json)) {
            try {
                return getPropsSerializer().deserialize(json);
            } catch (IOException e) {
                throw new DuraCloudRuntimeException(e);
            }
        }
        return null;
    }

    protected static String serializeContentProperties(Map<String, String> props) {
        if (props != null) {
            try {
                return getPropsSerializer().serialize(props);
            } catch (IOException e) {
                throw new DuraCloudRuntimeException(e);
            }
        }
        return null;
    }

    /*
     * Creates a serializer for properties
     * Note: The odd string of casts is necessary to make the command-line
     *       compiler happy.
     */
    private static JaxbJsonSerializer<Map<String, String>> getPropsSerializer() {
        return new JaxbJsonSerializer<>((Class<Map<String, String>>) (Object)
            new HashMap<String, String>().getClass());
    }

}
