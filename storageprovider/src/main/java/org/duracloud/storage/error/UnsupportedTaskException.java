/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.error;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class UnsupportedTaskException extends TaskException {

    /**
     * Indicates that a requested task is not supported
     * @param task name of task
     */
    public UnsupportedTaskException(String task) {
        super(task + " is not a supported task.");
    }

    /**
     * Indicates that the use of a task is not supported
     * @param task name of task
     * @param message reason for error
     */
    public UnsupportedTaskException(String task, String message) {
        super("Execution of task " + task +
              " is not supported as requested. " + message);
    }

}