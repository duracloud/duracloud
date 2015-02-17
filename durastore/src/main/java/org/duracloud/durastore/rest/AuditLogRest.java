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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.duracloud.audit.reader.AuditLogReader;
import org.duracloud.audit.reader.AuditLogReaderNotEnabledException;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * REST interface for the AuditLogReader.
 *
 * @author Daniel Bernstein
 *         Date: Sept 17, 2014
 */
@Path("/audit")
@Component
public class AuditLogRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(AuditLogRest.class);

    private AuditLogReader auditLogReader;
    private StorageProviderFactory storageProviderFactory;
    @Autowired
    public AuditLogRest(AuditLogReader auditLogReader, StorageProviderFactory storageProviderFactory) {
        this.auditLogReader = auditLogReader;
        this.storageProviderFactory = storageProviderFactory;
    }

    @Path("/{spaceId}")
    @GET
    public Response getAuditLog (@PathParam("spaceId") String spaceId,
                                 @QueryParam("storeID") String storeId) {
        
        String account = getSubdomain();
        
        log.info("getting audit log for account:{}, storeId:{}, spaceId:{}",
                 account,
                 storeId,
                 spaceId);

        if(StringUtils.isBlank(storeId)){
            for(StorageAccount storageAccount: this.storageProviderFactory.getStorageAccounts()){
                if(storageAccount.isPrimary()){
                    storeId = storageAccount.getId();
                    break;
                }
            }
            
            if(StringUtils.isBlank(storeId)){
                throw new DuraCloudRuntimeException("storeId is blank and no primary storage account is indicated.");
            }
            
        }

        
        try {
            //check that spaces exists
            StorageProvider store = storageProviderFactory.getStorageProvider(storeId);
            store.getSpaceProperties(spaceId);

            InputStream auditLog = auditLogReader.getAuditLog(account, storeId,
                                                                spaceId);
            return responseOkStream(auditLog);
        } catch (NotFoundException e) {
            
            log.error(MessageFormat.format("Error for  account:{0}, storeId:{1}, spaceId:{2}: space not found.",
                      account, storeId, spaceId), e);

            return responseNotFound(e.getMessage());
        } catch (AuditLogReaderNotEnabledException e) {
            
            log.error(MessageFormat.format("Error for  account:{0}, storeId:{1}, spaceId:{2}: space not found.",
                      account, storeId, spaceId), e);

            return Response.status(501).entity("This endpoint is currently disabled").build();

        } catch (Exception e) {
            
            log.error(MessageFormat.format("Error for  account:{0}, storeId:{1}, spaceId:{2}",
                      account, storeId, spaceId), e);
            return responseBad(e);
        }
    }

}
