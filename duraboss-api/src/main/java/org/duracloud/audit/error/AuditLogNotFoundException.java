/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * @author Andrew Woods
 *         Date: 3/19/12
 */
public class AuditLogNotFoundException extends DuraCloudCheckedException {

    public AuditLogNotFoundException(String spaceId) {
        super("No audit log found for: " + spaceId);
    }
}
