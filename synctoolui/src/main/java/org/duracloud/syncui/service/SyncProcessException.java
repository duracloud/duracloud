/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

/**
 * @author Daniel Bernstein
 */

public class SyncProcessException extends Exception {
    public SyncProcessException(String message, Throwable t) {
        super(message, t);
    }
}
