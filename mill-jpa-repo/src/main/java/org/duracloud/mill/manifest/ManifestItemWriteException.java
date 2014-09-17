/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mill.manifest;


/**
 * @author Daniel Bernstein
 *         Date: Sep 2, 2014
 */
public class ManifestItemWriteException extends Exception {
    /**
     * @param message
     * @param ex
     */
    public ManifestItemWriteException(String message, Exception ex) {
        super(message,ex);
    }
    
}
