/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.report.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * Exception thrown by the Report Manager.
 *
 * @author Bill Branan
 * Date: 6/2/11
 */
public class ReportException extends DuraCloudCheckedException {
    private static final long serialVersionUID = 1L;

    public ReportException(String message) {
        super(message);
    }

    public ReportException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ReportException(Throwable throwable) {
        super(throwable);
    }
}
