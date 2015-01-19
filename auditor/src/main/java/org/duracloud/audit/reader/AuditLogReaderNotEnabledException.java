/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.reader;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class AuditLogReaderNotEnabledException extends AuditLogReaderException {
    public AuditLogReaderNotEnabledException(){
        super("The audit log reader is not enabled.");
    }
}
