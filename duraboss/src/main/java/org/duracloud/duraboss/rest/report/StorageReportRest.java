/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest.report;

import java.io.InputStream;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.duracloud.duraboss.rest.BaseRest;
import org.duracloud.reporter.error.InvalidScheduleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * REST interface for storage reporting
 *
 * @author: Bill Branan
 * Date: 5/11/11
 */
@Path("/report/storage")
@Component
public class StorageReportRest extends BaseRest {

    private StorageReportResource resource;

    private final Logger log = LoggerFactory.getLogger(StorageReportRest.class);

    @Autowired
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
    public Response getLatestStorageReport(){
        log.debug("Getting the latest storage report");

        try {
            InputStream stream = resource.getLatestStorageReport();
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
            String xml = resource.getStorageReportList();
            return responseOkXml(xml);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    /**
     * Retrieves a storage report.
     *
     * @return 200 response with storage report XML as body
     */
    @Path("/{reportID: [^?]+}")
    @GET
    @Produces(XML)
    public Response getStorageReport(@PathParam("reportID")
                                     String reportId){
        log.debug("Getting storage report with ID: " + reportId);

        try {
            InputStream stream = resource.getStorageReport(reportId);
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
     * Retrieves information about storage reporting activities.
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

    /**
     * Cancels a running storage report.
     *
     * @return 200 response with body text indicating success
     */
    @DELETE
    public Response cancelStorageReport(){
        log.debug("Cancelling storage report");

        try {
            String responseText = resource.cancelStorageReport();
            return responseOk(responseText);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    /**
     * Sets up a schedule of storage reports to be run, based on a starting
     * time and the frequency at which the report should be run afterward.
     *
     * @return 200 response with body text indicating success
     */
    @Path("/schedule")
    @POST
    public Response scheduleStorageReport(@QueryParam("startTime")
                                          long startTime,
                                          @QueryParam("frequency")
                                          long frequency){
        log.debug("Scheduling a storage report series");

        try {
            String responseText =
                resource.scheduleStorageReport(startTime, frequency);
            return responseOk(responseText);
        } catch (InvalidScheduleException e) {
            return responseBadRequest(e);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    /**
     * Cancels the storage report schedule. This does not stop running
     * storage reports, only cancels those that would run in the future.
     *
     * @return 200 response with body text indicating success
     */
    @Path("/schedule")
    @DELETE
    public Response cancelStorageReportSchedule(){
        log.debug("Cancelling all scheduled storage reports");

        try {
            String responseText = resource.cancelStorageReportSchedule();
            return responseOk(responseText);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

}
