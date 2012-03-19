/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest.audit;

import org.duracloud.audit.Auditor;
import org.duracloud.audit.error.AuditLogNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author Andrew Woods
 *         Date: 3/17/12
 */
public class AuditResource {

    private final Logger log = LoggerFactory.getLogger(AuditResource.class);

    private Auditor auditor;

    public AuditResource(Auditor auditor) {
        this.auditor = auditor;
    }

    /**
     * This method requests the creation of an initial audit log.
     *
     * @return URL of newly created audit log
     */
    public void createInitialAuditLogs() {
        auditor.createInitialAuditLogs();
    }

    /**
     * This method requests auditor shutdown.
     */
    public void shutdownAuditor() {
        auditor.stop();
    }

    /**
     * This method returns the list of audit logs for the arg space.
     *
     * @param spaceId of the space over which auditing is requested
     * @return list of audit logs for the arg space
     */
    public String getAuditLogs(String spaceId)
        throws AuditLogNotFoundException {
        return auditor.getAuditLogs(spaceId);
    }
}
