/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.DateUtil;
import org.duracloud.reportdata.storage.SpaceStatsDTO;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.util.StorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * REST interface for the StorageStats
 *
 * @author Daniel Bernstein Date: 2/25/2016
 */
@Path("/storagestats")
@Component
public class StorageStatsRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(StorageStatsRest.class);

    private StorageStatsResource resource;
    private StorageProviderFactory storageProviderFactory;
    @Autowired
    public StorageStatsRest(StorageStatsResource resource, StorageProviderFactory storageProviderFactory) {
        this.resource = resource;
        this.storageProviderFactory = storageProviderFactory;
    }

    @Path("/{spaceId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public Response getSpaceStats(@PathParam("spaceId") String spaceId,
                                  @QueryParam("storeID") String storeId,
                                  @QueryParam("start") String start,
                                  @QueryParam("end") String end) {

        String account = getSubdomain();

        log.info("getting storage stats, {}:{}:{} [{}:{}]",
                 account,
                 storeId,
                 spaceId,
                 start,
                 end);

        try {
            
            Date startDate;
            if(null == start){
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -90);
                c.set(Calendar.HOUR, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                startDate = c.getTime();
            }else{
                startDate = DateUtil.convertToDate(start);
            }

            Date endDate = new Date();
            if(null != end){
                endDate = DateUtil.convertToDate(end);
            }

            if(storeId == null){
                List<StorageAccount> accounts = this.storageProviderFactory.getStorageAccounts();
                for(StorageAccount sa : accounts){
                    if(sa.isPrimary()){
                        storeId = sa.getId();
                        break;
                    }
                }
                
                if(storeId == null){
                    throw new DuraCloudRuntimeException("unable to resolve primary store id");
                }
                
            }
            
            List<SpaceStatsDTO> stats =
                resource.getSpaceStats(account, storeId, spaceId, startDate , endDate);

            
            return responseOk(stats);

        } catch (Exception e) {
            log.error(MessageFormat.format("error getting storage stats, {0}:{1}:{2} [{3}:{4}]",
                     account,
                     storeId,
                     spaceId,
                     start,
                     end), e);
            return responseBad(e);
        }
    }

}
