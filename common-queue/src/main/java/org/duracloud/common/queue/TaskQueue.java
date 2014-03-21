/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.queue;

import org.duracloud.common.queue.task.Task;

import java.util.Set;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public interface TaskQueue {

    /**
     * A name identifying the queue used for logging and analysis purposes.
     * @return
     */
    public String getName();
    
    /**
     * puts a task on the queue
     * 
     * @param task
     */
    public void put(Task task);

    /**
     * puts multiple tasks on the queue using batch puts if the queue
     * implementation supports batch puts
     * @param tasks
     */
    public void put(Task... tasks);

    /**
     * puts multiple tasks on the queue using batch puts if the queue
     * implementation supports batch puts
     * @param tasks
     */
    public void put(Set<Task> tasks);

    /**
     * Blocks until a task is available
     * 
     * @return
     */
    public Task take() throws TimeoutException;


    /**
     * Responsible for robustly extending the visibility timeout of a Task.
     * 
     * @param task
     * @throws TaskNotFoundException
     */
    public void extendVisibilityTimeout(Task task)
            throws TaskNotFoundException;

    /**
     * Deletes a task from the queue.
     * 
     * @param task
     */
    public void deleteTask(Task task) throws TaskNotFoundException;

    /**
     * @return the number of elements in this queue.
     */
    public Integer size();

    /**
     * Requeues the task by deleting the task, incrementing the "attempts" counter, and re-adding back to the queue.
     * Any subsequent calls on the requeued task via the task queue should fail due to the task not being found.
     * @param task
     */
    public void requeue(Task task);
}
