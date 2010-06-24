/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.error;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class TaskException extends DuraCloudRuntimeException {

    public TaskException(String message) {
        super(message);
    }

    public TaskException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
