/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.error;

/**
 * Represents a task exception associated with a server conflict.
 * @author dbernstein
 */
public class ServerConflictException extends TaskException{
    public ServerConflictException(String message){
        super(message);
    }
}
