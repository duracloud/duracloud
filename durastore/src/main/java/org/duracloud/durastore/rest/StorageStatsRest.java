/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.durastore.rest.StorageStatsResource.GroupBy;
import org.duracloud.error.NotFoundException;
import org.duracloud.reportdata.storage.SpaceStatsDTO;
import org.duracloud.reportdata.storage.StoreStatsDTO;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.provider.StorageProvider;
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
@Path("/report")
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

    @Path("/space/{spaceID}")
    @GET
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public Response getSpaceStatsOverTime(@PathParam("spaceID") String spaceId,
                                  @QueryParam("storeID") String storeId,
                                  @QueryParam("start") String startMs,
                                  @QueryParam("end") String endMs,
                                  @QueryParam(value="groupBy") String groupBy){

        String account = getSubdomain();

        log.info("getting storage stats, {}:{}:{} [{}:{}]",
                 account,
                 storeId,
                 spaceId,
                 startMs,
                 endMs);

        try {
            
            Date startDate = resolveStartDate(startMs);
            Date endDate = resolveEndDate(endMs);
            storeId = getStoreId(storeId);
            ensureSpaceIsValid(storeId, spaceId);
            List<SpaceStatsDTO> stats =
                resource.getSpaceStats(account,
                                       storeId,
                                       spaceId,
                                       startDate,
                                       endDate,
                                       getGroupBy(groupBy));
            return responseOk(stats);

        } catch (Exception e) {
            return handleException(e,
                                   MessageFormat.format("error getting storage stats, {0}:{1}:{2} [{3}:{4}]",
                                                        account,
                                                        storeId,
                                                        spaceId,
                                                        startMs,
                                                        endMs));
        }
    }

    private void ensureSpaceIsValid(String storeId, String spaceId) throws NotFoundException {
        StorageProvider provider = storageProviderFactory.getStorageProvider(storeId);
        try {
            provider.getSpaceProperties(spaceId);
        }catch(org.duracloud.storage.error.NotFoundException ex){
            throw new NotFoundException("The space was not found: " + spaceId);
        }
    }

    @Path("/store")
    @GET
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public Response getStoreStatsOverTime(@QueryParam("storeID") String storeId,
                                  @QueryParam("start") String startMs,
                                  @QueryParam("end") String endMs,
                                  @QueryParam(value="groupBy") String groupBy){

        String account = getSubdomain();

        log.info("getting storage stats, {}:{} [{}:{}]",
                 account,
                 storeId,
                 startMs,
                 endMs);

        try {
            Date startDate = resolveStartDate(startMs);
            Date endDate = resolveEndDate(endMs);
            storeId = getStoreId(storeId);
            List<StoreStatsDTO> stats =
                resource.getStorageProviderStats(account,
                                                 storeId,
                                                 startDate,
                                                 endDate,
                                                 getGroupBy(groupBy));
            return responseOk(stats);

        }catch (Exception e) {
            return handleException(e,
                                   MessageFormat.format("error getting storage stats, {0}:{1} [{3}:{4}]",
                                                        account,
                                                        storeId,
                                                        startMs,
                                                        endMs));
        }
    }

    private GroupBy getGroupBy(String groupBy) {
        if(groupBy == null){
            return GroupBy.day;
        }else{
            try {
                return GroupBy.valueOf(groupBy.toLowerCase());
            }catch(IllegalArgumentException ex){
                String message =
                    groupBy + " is not a valid value for the groupBy parameter.  You must specify one of the following values: "
                                 + StringUtils.join(GroupBy.values(), ",");
                throw new IllegalArgumentException(message,ex);
            }
        }
    }

    protected Date resolveEndDate(String endMs) {
        Date endDate = new Date();
        if(null != endMs){
            endDate = toDateFromMs(endMs);
        }
        return endDate;
    }

    protected Date resolveStartDate(String startMs) {
        Date startDate;
        if(null == startMs){
            startDate = new Date(0);
        }else{
            startDate = toDateFromMs(startMs);
        }
        return startDate;
    }

    protected Date toDateFromMs(String endMs) throws NumberFormatException {
        try {
            return new Date(Long.parseLong(endMs));
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Unable to parse date: " + endMs
                                            + ". Input value must be in epoch milliseconds.");
        }
    }
    
    @Path("/store/{date}")
    @GET
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public Response getStorageStatsByDay(@QueryParam("storeID") String storeId,
                                  @PathParam("date") String dateMs) {

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
            return handleException(e, MessageFormat.format("error getting storage stats, {0}:{1}:{2} [{3}]",
                                                    account,
                                                    storeId,
                                                    dateMs));
        }
    }

    private Response handleException(Exception e, String defaultErrorMessage) {
        if(e instanceof NumberFormatException || e instanceof IllegalArgumentException){
            log.error(e.getMessage(), e);
            return responseBad(e, Status.BAD_REQUEST);
        }else if(e instanceof NotFoundException){
            log.error(e.getMessage(), e);
            return responseBad(e, Status.NOT_FOUND);
        }else{
            log.error(defaultErrorMessage, e);
            return responseBad(e);
        }
    }

    protected String getStoreId(String storeId) throws NotFoundException {
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
        }else{
            List<StorageAccount> accounts = this.storageProviderFactory.getStorageAccounts();
            boolean valid = false;
            for(StorageAccount sa : accounts){
                if(storeId.equals(sa.getId())){
                    valid = true;
                    break;
                }
            }
            
            if(!valid){
                throw new NotFoundException("Store id (" + storeId
                                            + ") is not associated with this account.");
            }
            
        }
        return storeId;
    }
}
