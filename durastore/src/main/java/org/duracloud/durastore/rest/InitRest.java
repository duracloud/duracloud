/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.rest.RestUtil;
import org.duracloud.common.util.InitUtil;
import org.duracloud.storage.util.StorageProviderFactory;
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

    private StorageProviderFactory storageProviderFactory;
    private RestUtil restUtil;

    public InitRest(StorageProviderFactory storageProviderFactory,
                    RestUtil restUtil) {
        this.storageProviderFactory = storageProviderFactory;
        this.restUtil = restUtil;
    }

    /**
     * Initializes the instance. Expects as POST data
     * an XML file which includes credentials for all
     * available storage providers accounts.
     *
     * @return 200 on success
     */
    @POST
    public Response initialize(){
        String msg = "initializing " + APP_NAME;

        RestUtil.RequestContent content = null;
        try {
            String instanceHost = request.getServerName();
            String instancePort = String.valueOf(request.getServerPort());
            log.info("Initializing DuraStore on host: " + instanceHost +
                     " and port: " + instancePort);

            content = restUtil.getRequestContent(request, headers);
            storageProviderFactory.initialize(content.getContentStream(),
                                              instanceHost,
                                              instancePort);
            String responseText = "Initialization Successful";
            return responseOk(msg, responseText);

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    public Response isInitialized() {
        String msg = "checking initialized";

        boolean initialized = storageProviderFactory.isInitialized();
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
