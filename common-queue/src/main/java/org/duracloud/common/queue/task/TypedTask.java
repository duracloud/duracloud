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
 * Contains the base information necessary for tasks working with content items.
 *
 * @author Daniel Bernstein
 *         Date: 05/02/2014
 */
public abstract class TypedTask extends SpaceCentricTypedTask{

    public static final String CONTENT_ID_PROP = "contentId";

    private String contentId;


    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    @Override
    public void readTask(Task task) {
        super.readTask(task);
        Map<String, String> props = task.getProperties();
        setContentId(props.get(CONTENT_ID_PROP));
    }

    @Override
    public Task writeTask() {
        Task task = super.writeTask();
        addProperty(task, CONTENT_ID_PROP, getContentId());
        return task;
    }
}
