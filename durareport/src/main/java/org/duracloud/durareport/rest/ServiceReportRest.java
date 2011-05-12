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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
     * Retrieves the latest service report.
     *
     * @return 200 response with service report XML as body
     */
    @GET
    @Produces(XML)
    public Response getServiceReport(){
        log.debug("Getting service report");

        String xml = resource.getServiceReport();
        return responseOkXml(xml);
    }

}
