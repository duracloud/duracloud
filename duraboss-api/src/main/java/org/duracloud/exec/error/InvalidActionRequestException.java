/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * @author: Bill Branan
 * Date: 3/1/12
 */
public class InvalidActionRequestException extends DuraCloudCheckedException {

    public InvalidActionRequestException(String action) {
        super(action);
    }

}
