/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.task;

import org.duracloud.common.queue.task.Task;
import org.duracloud.common.queue.task.TypedTask;

import java.util.Map;

/**
 * A Task which will be used to capture an action taken on a
 * DuraCloud content item.
 *
 * @author Bill Branan
 *         Date: 3/14/14
 */
public class AuditTask extends TypedTask {

    public static final String ACTION_PROP = "action";
    public static final String USER_ID_PROP = "user-id";
    public static final String DATE_TIME_PROP = "date-time";
    public static final String CONTENT_CHECKSUM_PROP = "content-checksum";
    public static final String CONTENT_MIMETYPE_PROP = "content-mimetype";
    public static final String CONTENT_SIZE_PROP = "content-size";
    public static final String CONTENT_PROPERTIES_PROP = "content-properties";
    public static final String SPACE_ACLS_PROP = "space-acls";
    public static final String STORE_TYPE_PROP = "store-type";

    public enum ActionType {
        // Write actions
        CREATE_SPACE, DELETE_SPACE, SET_SPACE_ACLS, ADD_CONTENT,
        COPY_CONTENT, DELETE_CONTENT, SET_CONTENT_PROPERTIES,
        // Read actions
        GET_SPACES, GET_SPACE_CONTENTS, GET_SPACE_CONTENTS_CHUNKED,
        GET_SPACE_PROPERTIES, GET_SPACE_ACLS, GET_CONTENT,
        GET_CONTENT_PROPERTIES
    };

    private String action;
    private String userId;
    private String dateTime;
    private String contentChecksum;
    private String contentMimetype;
    private String contentSize;
    private String contentProperties;
    private String spaceACLs;
    private String storeType;

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

    public String getContentProperties() {
        return contentProperties;
    }

    public void setContentProperties(String contentProperties) {
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
        setContentProperties(props.get(CONTENT_PROPERTIES_PROP));
        setSpaceACLs(props.get(SPACE_ACLS_PROP));
        setStoreType(props.get(STORE_TYPE_PROP));
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
        addProperty(task, CONTENT_PROPERTIES_PROP, getContentProperties());
        addProperty(task, SPACE_ACLS_PROP, getSpaceACLs());
        addProperty(task, STORE_TYPE_PROP, getStoreType());
        return task;
    }

}
