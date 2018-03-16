/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * @author Andrew Woods
 * Date: Feb 8, 2010
 */
public class NotFoundException extends DuraCloudCheckedException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String spaceId, Exception e) {
        super(spaceId, e);
    }

}
