/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * REST interface for storage reporting
 *
 * @author: Bill Branan
 * Date: 5/11/11
 */
@Path("/storagereport")
public class StorageReportRest extends BaseRest {

    private StorageReportResource resource;

    private final Logger log = LoggerFactory.getLogger(StorageReportRest.class);

    public StorageReportRest(StorageReportResource resource) {
        this.resource = resource;
    }

    /**
     * Retrieves the latest completed storage report.
     *
     * @return 200 response with storage report XML as body
     */
    @GET
    @Produces(XML)
    public Response getStorageReport(){
        log.debug("Getting storage report");

        try {
            InputStream stream = resource.getStorageReport();
            if(null != stream) {
                return responseOkXmlStream(stream);
            } else {
                return responseNotFound();
            }
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    /**
     * Retrieves the list of storage report IDs.
     *
     * @return 200 response with storage report list XML as body
     */
    @Path("/list")
    @GET
    @Produces(XML)
    public Response getStorageReportList(){
        log.debug("Getting storage report list");

        try {
            InputStream stream = resource.getStorageReportList();
            if(null != stream) {
                return responseOkXmlStream(stream);
            } else {
                return responseNotFound();
            }
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    /**
     * Retrieves information about storage reporting activities
     *
     * @return 200 response with storage report info XML as body
     */
    @Path("/info")
    @GET
    @Produces(XML)
    public Response getStorageReportInfo(){
        log.debug("Getting storage report info");

        try {
            String xml = resource.getStorageReportInfo();
            return responseOkXml(xml);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    /**
     * Indicates that a new storage report should be started. If a storage
     * report is currently running, this request is ignored.
     *
     * @return 200 response with body text indicating success
     */
    @POST
    public Response startStorageReport(){
        log.debug("Starting storage report");

        try {
            String responseText = resource.startStorageReport();
            return responseOk(responseText);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

}
