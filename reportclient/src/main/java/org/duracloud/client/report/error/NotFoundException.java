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
 * Exception thrown when a response from a report call returns a
 * 404 NOT FOUND response. The message of the exception should indicate
 * which part of the call was not found.
 *
 * @author Bill Branan
 * Date: 6/2/11
 */
public class NotFoundException extends DuraCloudCheckedException {
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
