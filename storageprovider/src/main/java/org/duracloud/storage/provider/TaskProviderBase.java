/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.provider;

import org.duracloud.storage.error.UnsupportedTaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: 2/1/13
 */
public abstract class TaskProviderBase implements TaskProvider {

    protected Logger log = LoggerFactory.getLogger(TaskProviderBase.class);

    protected List<TaskRunner> taskList = new ArrayList<>();

    private String storeId;

    public TaskProviderBase(String storeId) {
        if(storeId == null){
            throw new IllegalArgumentException("storeId must be non-null");
        }
        this.storeId = storeId;
    }
    
    @Override
    public String getStoreId() {
        return this.storeId;
    }
    
    @Override
    public List<String> getSupportedTasks() {
        log.debug("getSupportedTasks()");

        List<String> supportedTasks = new ArrayList<>();
        for(TaskRunner runner : taskList) {
            supportedTasks.add(runner.getName());
        }
        return supportedTasks;
    }

    @Override
    public String performTask(String taskName, String taskParameters) {
        log.debug("performTask(" + taskName + ", " + taskParameters + ")");

        for(TaskRunner runner : taskList) {
            if(runner.getName().equals(taskName)) {
                return runner.performTask(taskParameters);
            }
        }
        throw new UnsupportedTaskException(taskName);
    }

}
