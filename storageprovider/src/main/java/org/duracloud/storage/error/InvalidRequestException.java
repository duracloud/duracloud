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
 * @author Andrew Woods
 *         Date: 8/16/11
 */
public class InvalidRequestException extends DuraCloudCheckedException {

    public InvalidRequestException(String msg) {
        super(msg);
    }
}
