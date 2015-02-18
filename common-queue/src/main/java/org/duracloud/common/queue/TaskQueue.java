/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.queue;

import java.util.Set;

import org.duracloud.common.queue.task.Task;

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
     * Take a max of specified number of tasks. Blocks until at least one task
     * is available.
     * 
     * @param maxTasks
     *            to take from queue. Must be between 1 and 10 inclusive.
     * @return
     * @throws TimeoutException
     */
    public Set<Task> take(int maxTasks) throws TimeoutException;

    
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
     * Deletes a set of tasks
     * 
     * @param task
     */
    public void deleteTasks(Set<Task> tasks) throws TaskException;

    /**
     * @return The approximate number of elements in this queue (does not
     *         include invisible and delayed tasks).
     */
    public Integer size();

    /**
     * @return The approximate number of elements in this queue including
     *         all items that are visible (available for takes),
     *         invisible (in process - not yet completed), and delayed (pending
     *         addition to the queue).
     */
    public Integer sizeIncludingInvisibleAndDelayed();

    /**
     * Requeues the task by deleting the task, incrementing the "attempts"
     * counter, and re-adding back to the queue. Any subsequent calls on the
     * requeued task via the task queue should fail due to the task not being
     * found.
     * 
     * @param task
     */
    public void requeue(Task task);
}
