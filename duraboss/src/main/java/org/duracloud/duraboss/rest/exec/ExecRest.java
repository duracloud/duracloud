/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest.exec;

import org.duracloud.common.rest.RestUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.duraboss.rest.BaseRest;
import org.duracloud.exec.error.InvalidActionRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * REST interface for the Executor
 *
 * @author: Bill Branan
 * Date: 3/1/12
 */
@Path("/exec")
public class ExecRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(ExecRest.class);

    ExecResource execResource;
    RestUtil restUtil;

    public ExecRest(ExecResource execResource, RestUtil restUtil) {
        this.execResource = execResource;
        this.restUtil = restUtil;
    }

    @GET
    public Response getExecutorStatus() {
        log.debug("Getting executor status");

        try {
            String xml = execResource.getExecutorStatus();
            return responseOkXml(xml);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    @Path("/action")
    @GET
    public Response getSupportedActions() {
        log.debug("Getting supported executor actions");

        try {
            String xml = execResource.getSupportedActions();
            return responseOkXml(xml);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    @Path("/{actionName}")
    @POST
    public Response performAction(@PathParam("actionName")
                                  String actionName){
        log.debug("Performing action status");

        try {
            String actionParams = getActionParameters();
            execResource.performAction(actionName, actionParams);
            return responseOk();
        } catch (InvalidActionRequestException e) {
            return responseBadRequest(e);
        } catch (Exception e) {
            return responseBad(e);
        }
    }

    private String getActionParameters() throws Exception {
        String actionParams = null;
        RestUtil.RequestContent content =
            restUtil.getRequestContent(request, headers);

        if(content != null) {
            InputStream contentStream = content.getContentStream();
            if(contentStream != null) {
                actionParams = IOUtil.readStringFromStream(contentStream);
            }
        }

        return actionParams;
    }

    @DELETE
    public Response shutdownExecutor() {
        log.debug("Shutting down executor");

        try {
            execResource.shutdownExecutor();
            return responseOk();
        } catch (Exception e) {
            return responseBad(e);
        }
    }

}
