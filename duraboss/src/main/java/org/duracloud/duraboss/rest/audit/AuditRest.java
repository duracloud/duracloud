/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest.audit;

import org.duracloud.audit.error.AuditLogNotFoundException;
import org.duracloud.duraboss.rest.BaseRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * REST interface for the Auditor.
 *
 * @author Andrew Woods
 *         Date: 3/17/12
 */
@Path("/audit")
public class AuditRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(AuditRest.class);

    private AuditResource auditResource;

    public AuditRest(AuditResource auditResource) {
        this.auditResource = auditResource;
    }

    @POST
    public Response createInitialAuditLog() {
        log.debug("Creating initial audit log");

        try {
            auditResource.createInitialAuditLogs();
            return Response.status(Response.Status.ACCEPTED).build();

        } catch (Exception e) {
            return responseBad(e);
        }
    }

    @Path("/{spaceId}")
    @GET
    public Response getAuditLogs(@PathParam("spaceId") String spaceId) {
        try {
            String logs = auditResource.getAuditLogs(spaceId);
            return responseOk(logs);

        } catch (AuditLogNotFoundException alnfe) {
            return responseNotFound(alnfe.getMessage());
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    @DELETE
    public Response shutdownAuditor() {
        log.debug("Shutting down auditor");

        try {
            auditResource.shutdownAuditor();
            return responseOk("auditor shutting down");

        } catch (Exception e) {
            return responseBad(e);
        }
    }

}
