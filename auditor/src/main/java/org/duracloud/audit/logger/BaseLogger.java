/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.logger;

import org.duracloud.audit.task.AuditTask;
import org.duracloud.common.queue.task.Task;

import java.util.Map;

/**
 * @author Bill Branan
 *         Date: 3/21/14
 */
public abstract class BaseLogger {

    protected String buildLogMessage(Task task) {
        StringBuilder builder = new StringBuilder();
        Map<String, String> props = task.getProperties();
        // Add action first
        add(builder, AuditTask.ACTION_PROP, props.remove(AuditTask.ACTION_PROP));
        // Add other properties
        for(String key : props.keySet()) {
            add(builder, key, props.get(key));
        }
        return builder.toString();
    }

    private void add(StringBuilder builder, String key, String value) {
        builder.append(key).append("=").append(value).append(" ");
    }

}
