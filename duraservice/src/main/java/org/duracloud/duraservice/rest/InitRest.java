/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.duracloud.common.rest.RestUtil;
import org.duracloud.common.util.InitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

/**
 * @author: Bill Branan
 * Date: 9/19/11
 */
@Path("/init")
public class InitRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(InitRest.class);

    private ServiceResource serviceResource;
    private RestUtil restUtil;

    public InitRest(ServiceResource serviceResource, RestUtil restUtil) {
        this.serviceResource = serviceResource;
        this.restUtil = restUtil;
    }

    /**
     * Initializes DuraService.
     * POST content should be similar to:
     *
     * <servicesConfig>
     *   <userStorage>
     *     <host>[USER-STORAGE-HOST-NAME]</host>
     *     <port>[USER-STORAGE-PORT]</port>
     *     <context>[USER-STORAGE-CONTEXT]</context>
     *     <msgBrokerUrl>[USER-STORAGE-MSG-BROKER-URL]</msgBrokerUrl>
     *   </userStorage>
     *   <serviceStorage>
     *     <host>[SERVICES-STORAGE-HOST-NAME]</host>
     *     <port>[SERVICES-STORAGE-PORT]</port>
     *     <context>[SERVICES-STORAGE-CONTEXT]</context>
     *     <spaceId>[SERVICES-STORAGE-SPACE-ID]</spaceId>
     *   </serviceStorage>
     *   <serviceCompute>
     *     <type>AMAZON_EC2</type>
     *     <imageId>[MACHINE-IMAGE-ID]</imageId>
     *     <computeProviderCredential>
     *       <username>[USERNAME]</username>
     *       <password>[PASSWORD]</password>
     *     </computeProviderCredential>
     *   </serviceCompute>
     * </servicesConfig>
     *
     * @return 200 on success
     */
    @POST
    public Response initializeServices() {
        String msg = "initializing " + APP_NAME;

        try {
            RestUtil.RequestContent content =
                restUtil.getRequestContent(request, headers);
            serviceResource.configureManager(content.getContentStream());
            String responseText = "Initialization Successful";
            return responseOk(msg, responseText);

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    public Response isInitialized() {
        String msg = "checking initialized";

        boolean initialized = serviceResource.isConfigured();
        if(initialized) {
            String text = InitUtil.getInitializedText(APP_NAME);
            return responseOk(msg, text);
        } else {
            String text = InitUtil.getNotInitializedText(APP_NAME);
            return responseBad(msg, text, SERVICE_UNAVAILABLE);
        }
    }

    private Response responseOk(String msg, String text) {
        log.debug(msg);
        return Response.ok(text, TEXT_PLAIN).build();
    }

    private Response responseBad(String msg,
                                 Exception e,
                                 Response.Status status) {
        String text = e.getMessage() == null ? "null" : e.getMessage();
        return responseBad(msg, text, status);
    }

    private Response responseBad(String msg,
                                 String text,
                                 Response.Status status) {
        log.error("Error while " + msg + ": " + text);
        return Response.status(status).entity(text).build();
    }

}
