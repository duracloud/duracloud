/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * @author: Bill Branan
 * Date: 6/24/11
 */
public class ServiceSummaryException extends DuraCloudCheckedException {

    private static final long serialVersionUID = 1L;

    public ServiceSummaryException(String message) {
        super(message);
    }

    public ServiceSummaryException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
