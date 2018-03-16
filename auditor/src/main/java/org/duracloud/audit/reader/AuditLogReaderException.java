/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.reader;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * @author Daniel Bernstein
 */
public class AuditLogReaderException extends DuraCloudRuntimeException {

    private static final long serialVersionUID = 1L;

    public AuditLogReaderException(String msg) {
        super(msg);
    }

    public AuditLogReaderException(Exception e) {
        super(e);
    }
}
