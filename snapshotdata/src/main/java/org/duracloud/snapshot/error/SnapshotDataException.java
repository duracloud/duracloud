/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.error;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * @author Bill Branan
 *         Date: 7/28/14
 */
public class SnapshotDataException extends DuraCloudRuntimeException {

    public SnapshotDataException(String message) {
        super(message);
    }

    public SnapshotDataException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
