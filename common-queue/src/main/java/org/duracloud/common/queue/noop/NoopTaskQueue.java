/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.queue.noop;

import org.duracloud.common.queue.TaskException;
import org.duracloud.common.queue.TaskNotFoundException;
import org.duracloud.common.queue.TaskQueue;
import org.duracloud.common.queue.TimeoutException;
import org.duracloud.common.queue.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Implementation of TaskQueue which performs no work. This is intended to be
 * used when the need for a fully functional TaskQueue implemenation is
 * deemed unnecessary.
 *
 * @author Bill Branan
 *         Date: 3/18/14
 */
public class NoopTaskQueue implements TaskQueue {

    public Logger log = LoggerFactory.getLogger(NoopTaskQueue.class);

    public NoopTaskQueue() {
        log.warn("USING A NOOP TASK QUEUE. ALL ITEMS PLACED ON THIS 'QUEUE' " +
                 "WILL BE LOST.");
    }

    @Override
    public String getName() {
        return "noop";
    }
    
    @Override
    public void put(Task task) {
    }

    @Override
    public void put(Task... tasks) {
    }

    @Override
    public void put(Set<Task> tasks) {
    }

    @Override
    public Task take() throws TimeoutException {
        return null;
    }

    @Override
    public void extendVisibilityTimeout(Task task)
        throws TaskNotFoundException {
    }

    @Override
    public void deleteTask(Task task) throws TaskNotFoundException {
    }

    @Override
    public Integer size() {
        return new Integer(0);
    }
    
    @Override
    public Integer sizeIncludingInvisibleAndDelayed() {
        return size();
    }

    @Override
    public void requeue(Task task) {
    }

    @Override
    public void deleteTasks(Set<Task> tasks) throws TaskException {}
    
    @Override
    public Set<Task> take(int maxTasks) throws TimeoutException {
        throw new TimeoutException("not implemented");
    }
}
