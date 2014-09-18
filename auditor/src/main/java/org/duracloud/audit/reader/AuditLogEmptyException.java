/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.reader;

import org.duracloud.common.error.DuraCloudCheckedException;
/**
 * 
 * @author Daniel Bernstein
 *         Date: Sept. 17, 2014
 *
 */
public class AuditLogEmptyException extends DuraCloudCheckedException {
    public AuditLogEmptyException(String msg){
        super(msg);
    }
}
