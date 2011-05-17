/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.error;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * @author: Bill Branan
 * Date: 5/16/11
 */
public class ReportBuilderException extends DuraCloudRuntimeException {

    public ReportBuilderException(String message) {
        super(message);
    }

    public ReportBuilderException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
}
