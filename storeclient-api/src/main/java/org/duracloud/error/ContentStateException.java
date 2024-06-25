/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.error;

/**
 * Exception thrown when a requested content item does not have the proper
 * state to allow for the requested action.
 *
 * @author Bill Branan
 * Date: Jan 31, 2013
 */
public class ContentStateException extends ContentStoreException {

    public ContentStateException(String message) {
        super(message);
    }

}