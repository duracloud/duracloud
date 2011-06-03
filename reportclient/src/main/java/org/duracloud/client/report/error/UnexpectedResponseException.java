/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.report.error;

/**
 * Exception thrown when an unexpected response code is returned from
 * a report call.
 *
 * @author Bill Branan
 * Date: 6/2/11
 */
public class UnexpectedResponseException extends ReportException {
    private static final long serialVersionUID = 1L;
    private static final String messageKey =
        "duracloud.error.reportclient.unexpectedresponse";

    public UnexpectedResponseException(String defaultMessage,
                                       int actualCode,
                                       int expectedCode,
                                       String msg) {
        super(defaultMessage);
        setArgs(new Integer(actualCode).toString(),
                new Integer(expectedCode).toString(),
                msg);
    }
}
