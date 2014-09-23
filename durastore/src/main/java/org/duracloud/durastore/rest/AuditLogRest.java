/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import java.io.InputStream;
import java.text.MessageFormat;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.duracloud.audit.reader.AuditLogEmptyException;
import org.duracloud.audit.reader.AuditLogReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST interface for the AuditLogReader.
 *
 * @author Daniel Bernstein
 *         Date: Sept 17, 2014
 */
@Path("/audit")
public class AuditLogRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(AuditLogRest.class);

    private AuditLogReader auditLogReader;

    public AuditLogRest(AuditLogReader auditLogReader) {
        this.auditLogReader = auditLogReader;
    }

    @Path("/{storeId}/{spaceId}")
    @GET
    public Response getAuditLog (@PathParam("spaceId") String spaceId,
                                @PathParam("storeId") String storeId) {
        
        String account = request.getHeader("X-FORWARDED-HOST");
        if(account == null){
            account = request.getServerName();
        }
        
        account = account.split("[.]")[0];
        
        log.info("getting audit log for account:{}, storeId:{}, spaceId:{}",
                 new Object[]{account, storeId, spaceId});

        try {
            InputStream auditLog = auditLogReader.gitAuditLog(account, storeId,
                                                                spaceId);
            return responseOkStream(auditLog);
        } catch (Exception e) {
            
            log.error(MessageFormat.format("Error for  account:{0}, storeId:{1}, spaceId:{2}",
                      account, storeId, spaceId), e);
            
            if(e instanceof AuditLogEmptyException){
                return responseNotFound("No audit logs found.");
            }else{
                return responseBad(e);
            }
        }
    }

}
