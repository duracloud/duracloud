/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.queue;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
import org.duracloud.common.error.DuraCloudCheckedException;

public class TaskException extends DuraCloudCheckedException {
    public TaskException() {
        super();
    }
    
    public TaskException(Throwable t){
        super(t);
    }

    public TaskException(String message){
        super(message);
    }

    public TaskException(String message, Throwable t){
        super(message,t);
    }

}

