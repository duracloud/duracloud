/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.queue.task;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 * Contains the base information necessary to handle tasks.
 *
 * @author Bill Branan
 *         Date: 10/24/13
 */
public abstract class TypedTask {

    public static final String ACCOUNT_PROP = "account";
    public static final String STORE_ID_PROP = "storeId";
    public static final String SPACE_ID_PROP = "spaceId";
    public static final String CONTENT_ID_PROP = "contentId";

    private String account;
    private String storeId;
    private String spaceId;
    private String contentId;

    /**
     * The unique identifier for the account, ie the account's subdomain.
     * 
     * @return
     */
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Reads the information stored in a Task and sets data in the TypedTask
     * @param task
     */
    public void readTask(Task task) {
        Map<String, String> props = task.getProperties();
        setAccount(props.get(ACCOUNT_PROP));
        setStoreId(props.get(STORE_ID_PROP));
        setSpaceId(props.get(SPACE_ID_PROP));
        setContentId(props.get(CONTENT_ID_PROP));
    }

    /**
     * Writes all of the information in the TypedTask into a Task
     * @return a Task based on the information stored in this TypedTask
     */
    public Task writeTask() {
        Task task = new Task();
        task.addProperty(ACCOUNT_PROP, getAccount());
        task.addProperty(STORE_ID_PROP, getStoreId());
        task.addProperty(SPACE_ID_PROP, getSpaceId());
        task.addProperty(CONTENT_ID_PROP, getContentId());
        return task;
    }

}
