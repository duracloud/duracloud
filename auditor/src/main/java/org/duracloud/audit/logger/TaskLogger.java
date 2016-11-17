/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.logger;

import java.util.LinkedHashMap;
import java.util.Map;

import org.duracloud.audit.task.AuditTask;
import org.duracloud.common.queue.task.Task;

/**
 * 
 * @author dbernstein
 *
 */
public abstract class TaskLogger extends BaseLogger {

    protected String buildLogMessage(Task task) {
        Map<String, String> props = task.getProperties();
        // Ensure action appears first in iteration of kv pairs
        String action = props.remove(AuditTask.ACTION_PROP);
        Map<String,String> orderedMap = new LinkedHashMap<>();
        orderedMap.put(AuditTask.ACTION_PROP, action);
        for(String key : props.keySet()){
            orderedMap.put(key, props.get(key));
        }
        return buildLogMessage(orderedMap);
    }

}
