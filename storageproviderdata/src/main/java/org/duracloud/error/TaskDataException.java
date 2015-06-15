/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.error;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * @author Bill Branan
 *         Date: 3/5/15
 */
public class TaskDataException extends DuraCloudRuntimeException {

    public TaskDataException(String message) {
        super(message);
    }

    public TaskDataException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
