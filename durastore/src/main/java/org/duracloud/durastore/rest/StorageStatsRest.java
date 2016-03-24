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

    @Path("/timeseries")
    @GET
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public Response getSpaceStatsOverTime(@QueryParam("spaceID") String spaceId,
                                  @QueryParam("storeID") String storeId,
                                  @QueryParam("start") String startMs,
                                  @QueryParam("end") String endMs) {

        String account = getSubdomain();

        log.info("getting storage stats, {}:{}:{} [{}:{}]",
                 account,
                 storeId,
                 spaceId,
                 startMs,
                 endMs);

        try {
            
            Date startDate;
            if(null == startMs){
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -90);
                c.set(Calendar.HOUR, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                startDate = c.getTime();
            }else{
                startDate = toDateFromMs(startMs);
            }

            Date endDate = new Date();
            if(null != endMs){
                endDate = toDateFromMs(endMs);
            }

            storeId = getStoreId(storeId);
            
            List<SpaceStatsDTO> stats; 
            
            if(spaceId == null) {
                stats =
                    resource.getStorageProviderStats(account, storeId, startDate , endDate);
            }else{
                stats =
                    resource.getSpaceStats(account, storeId, spaceId, startDate , endDate);
            }
            
            return responseOk(stats);

        } catch (Exception e) {
            log.error(MessageFormat.format("error getting storage stats, {0}:{1}:{2} [{3}:{4}]",
                     account,
                     storeId,
                     spaceId,
                     startMs,
                     endMs), e);
            return responseBad(e);
        }
    }

    protected Date toDateFromMs(String endMs) {
        return new Date(Long.parseLong(endMs));
    }

    
    @Path("/snapshot-by-day")
    @GET
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public Response getStorageStatsByDay(@QueryParam("storeID") String storeId,
                                  @QueryParam("date") String dateMs) {

        String account = getSubdomain();

        log.info("getting storage stats, {}:{}:{} [{}]",
                 account,
                 storeId,
                 dateMs);

        try {
            Date theDate =  toDateFromMs(dateMs);
            storeId = getStoreId(storeId);
            List<SpaceStatsDTO> stats =
                resource.getStorageProviderByDay(account, storeId, theDate);
            return responseOk(stats);
        } catch (Exception e) {
            log.error(MessageFormat.format("error getting storage stats, {0}:{1}:{2} [{3}]",
                     account,
                     storeId,
                     dateMs), e);
            return responseBad(e);
        }
    }

    protected String getStoreId(String storeId) {
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
        return storeId;
    }
}
