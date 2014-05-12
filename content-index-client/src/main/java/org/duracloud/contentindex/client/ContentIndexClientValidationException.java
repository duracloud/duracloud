/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.client;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class ContentIndexClientValidationException extends DuraCloudCheckedException {
    public ContentIndexClientValidationException(String message){
        super(message);
    }
}
