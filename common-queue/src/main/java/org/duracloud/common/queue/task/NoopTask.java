/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.queue.task;

import java.util.Map;

/**
 * @author Daniel Bernstein
 * Date: Oct 24, 2013
 */
public class NoopTask extends TypedTask {
    @Override
    public void readTask(Task task) {
        super.readTask(task);
        Map<String, String> props = task.getProperties();
    }

    @Override
    public Task writeTask() {
        Task task = super.writeTask();
        task.setType(Task.Type.NOOP);
        return task;
    }
}
