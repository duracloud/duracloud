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
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.StringUtils;
import org.duracloud.client.HttpHeaders;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.DateUtil;
import org.duracloud.mill.db.model.BitIntegrityReport;
import org.duracloud.mill.db.repo.JpaBitIntegrityReportRepo;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * REST interface for serving bit integrity reports.
 *
 * @author Daniel Bernstein
 *         Date: Oct 31, 2014
 */
@Path("/bit-integrity")
@Component
@Order(1)
public class BitIntegrityReportRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(BitIntegrityReportRest.class);

    private JpaBitIntegrityReportRepo repo;
    private StorageProviderFactory storageProviderFactory;
    @Autowired
    public BitIntegrityReportRest(JpaBitIntegrityReportRepo repo,  StorageProviderFactory storageProviderFactory) {
        this.repo = repo;
        this.storageProviderFactory = storageProviderFactory;
        log.info("created: {}, {}.", this.repo, this.storageProviderFactory);
    }

    @Path("/{spaceId}")
    @GET
    public Response getReport (@PathParam("spaceId") String spaceId,
                                 @QueryParam("storeID") String storeId) {
        log.debug("getting report for {} , {} ", spaceId, storeId);

        return getReport(spaceId, storeId, false);
    }

    @Path("/{spaceId}")
    @HEAD
    public Response getReportHead (@PathParam("spaceId") String spaceId,
                                 @QueryParam("storeID") String storeId) {
        log.debug("getting report head for {} , {} ", spaceId, storeId);
        return getReport(spaceId, storeId, true);
    }

    private Response getReport(String spaceId,
                                 String storeId,
                                 boolean headOnly) {
        String account = getSubdomain();
        
        log.info("getting bit integrity report log for account:{}, storeId:{}, spaceId:{}",
                 new Object[]{account, storeId, spaceId});

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
            PageRequest pageRequest = new PageRequest(0, 1);
            Page<BitIntegrityReport> page = repo.findByStoreIdAndSpaceIdAndDisplayTrueOrderByCompletionDateDesc(storeId, spaceId, pageRequest);
            
            if(page == null || CollectionUtils.isEmpty(page.getContent())){
                return responseBad("No reports matching the criteria found.", Response.Status.NO_CONTENT);
            }
            
            BitIntegrityReport report = page.getContent().get(0);
            //retrieve report info from primary store.
            StorageProvider provider = this.storageProviderFactory.getStorageProvider();
            String reportSpaceId = report.getReportSpaceId();
            String reportContentId = report.getReportContentId();
            Map<String,String> props = provider.getContentProperties(reportSpaceId, reportContentId);
            String contentLength = props.get(StorageProvider.PROPERTIES_CONTENT_SIZE);
            
            ResponseBuilder responseBuilder;
            
            if(headOnly){
                responseBuilder = Response.ok();
            }else{
                InputStream is = provider.getContent(reportSpaceId, reportContentId);
                responseBuilder = Response.ok(is);
            }
            
            responseBuilder.header(HttpHeaders.BIT_INTEGRITY_REPORT_RESULT, report.getResult().name());
            responseBuilder.header(HttpHeaders.BIT_INTEGRITY_REPORT_COMPLETION_DATE,
                                   DateUtil.convertToString(report.getCompletionDate()
                                                                  .getTime()));
            responseBuilder.header(HttpHeaders.CONTENT_LENGTH, contentLength);
            
            return responseBuilder.build();
            
        } catch (Exception e) {
            log.error(MessageFormat.format("Error for  account:{0}, storeId:{1}, spaceId:{2} -> {3}",
                      account, storeId, spaceId, e.getMessage()));
            return responseBad(e);
        }
    }

}
