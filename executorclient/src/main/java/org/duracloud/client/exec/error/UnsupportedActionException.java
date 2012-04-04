/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.exec.error;

/**
 * Indicates that the action that was asked to be performed is not supported
 * by the Executor.
 *
 * @author: Bill Branan
 * Date: 4/4/12
 */
public class UnsupportedActionException extends ExecutorException {

    public UnsupportedActionException(String message) {
        super(message);
    }

}
