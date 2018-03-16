/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.queue;

/**
 * @author Daniel Bernstein
 */
public class TaskNotFoundException extends TaskException {

    public TaskNotFoundException() {
        super();
    }

    public TaskNotFoundException(String message) {
        super(message);
    }

    public TaskNotFoundException(Throwable throwable) {
        super(throwable);
    }

    public TaskNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
