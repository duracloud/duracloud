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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
@Path("/servicereport")
public class ServiceReportRest extends BaseRest {

    private ServiceReportResource resource;

    private final Logger log = LoggerFactory.getLogger(ServiceReportRest.class);

    public ServiceReportRest(ServiceReportResource resource) {
        this.resource = resource;
    }

    /**
     * Retrieves the deployed services report.
     *
     * @return 200 response with service report XML as body
     */
    @Path("/deployed")
    @GET
    @Produces(XML)
    public Response getDeployedServicesReport(){
        log.debug("Getting deployed services report");

        try {
            InputStream stream = resource.getDeployedServicesReport();
            return responseOkXmlStream(stream);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    /**
     * Retrieves a report of completed services, including as many services
     * as have completed up to the provided limit. If no limit is provided,
     * the default limit of 20 is used.
     *
     * @return 200 response with service report XML as body
     */
    @GET
    @Produces(XML)
    public Response getCompletedServicesReport(@QueryParam("limit")
                                               int limit) {
        log.debug("Getting completed services report with limit: " + limit);

        try {
            InputStream stream = resource.getCompletedServicesReport(limit);
            return responseOkXmlStream(stream);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    /**
     * Retrieves the list of all service report files which have been created.
     *
     * @return 200 response with service report IDs XML as body
     */
    @Path("/list")
    @GET
    @Produces(XML)
    public Response getCompletedServicesReportList() {
        log.debug("Getting completed services report IDs");

        try {
            InputStream stream = resource.getCompletedServicesReportList();
            return responseOkXmlStream(stream);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    /**
     * Retrieves a specific service report by ID.
     *
     * @return 200 response with service report XML as body
     */
    @Path("/{reportId: [^?]+}")
    @GET
    @Produces(XML)
    public Response getCompletedServicesReport(@PathParam("reportId")
                                               String reportId) {
        log.debug("Getting completed services report by ID: " + reportId);

        try {
            InputStream stream = resource.getCompletedServicesReport(reportId);
            return responseOkXmlStream(stream);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

}
