/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.error;

import org.duracloud.common.error.DuraCloudCheckedException;
import org.duracloud.storage.error.NotFoundException;

/**
 * @author Andrew Woods
 *         Date: 6/24/11
 */
public class ServiceSummaryNotFoundException extends DuraCloudCheckedException {

    public ServiceSummaryNotFoundException(String msg, Throwable e) {
        super(msg, e);
    }
}
