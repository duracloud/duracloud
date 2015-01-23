/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.error;

/**
 * Exception thrown when a feature or method is not implemented.
 *
 * @author Daniel Bernstein
 */
public class NotImplementedException extends ContentStoreException {

    public NotImplementedException (String message) {
        super(message);
    }
}