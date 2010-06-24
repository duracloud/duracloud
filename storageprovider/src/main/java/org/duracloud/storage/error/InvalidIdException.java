/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * @author: Bill Branan
 * Date: Jan 11, 2010
 */
public class InvalidIdException extends DuraCloudCheckedException {

    public InvalidIdException(String message) {
        super(message);
    }
}