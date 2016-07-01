/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.duracloud.StorageTaskConstants;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.error.UnauthorizedException;
import org.duracloud.storage.error.StorageStateException;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.duracloud.storage.provider.TaskProvider;
import org.duracloud.storage.provider.TaskProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
/**
 * Allows for calling storage provider specific tasks
 *
 * @author Bill Branan
 *         Date: May 20, 2010
 */
@Path(StorageTaskConstants.TASK_BASE_PATH)
@Component
public class TaskRest extends BaseRest {

    private final Logger log = LoggerFactory.getLogger(TaskRest.class);

    private TaskProviderFactory taskProviderFactory;
    private RestUtil restUtil;

    @Autowired
    
    public TaskRest(@Qualifier("taskProviderFactory") TaskProviderFactory taskProviderFactory,
                    RestUtil restUtil) {
        this.taskProviderFactory = taskProviderFactory;
        this.restUtil = restUtil;
    }

    /**
     * Gets a listing of supported tasks for a given provider
     *
     * @return 200 on success
     */
    @GET
    public Response getSupportedTasks(@QueryParam("storeID")
                                      String storeID){
        String msg = "getting suppported tasks(" + storeID + ")";
        try {
            TaskProvider taskProvider =
                taskProviderFactory.getTaskProvider(storeID);

            List<String> supportedTasks = taskProvider.getSupportedTasks();
            String responseText =
                SerializationUtil.serializeList(supportedTasks);

            return responseOkXml(msg, responseText);
        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Performs a task
     *
     * @return 200 on success
     */
    @Path("/{taskName}")
    @POST
    public Response performTask(@PathParam("taskName")
                                String taskName,
                                @QueryParam("storeID")
                                String storeID){
        String msg = "performing task(" + taskName + ", " + storeID + ")";

        String taskParameters = null;
        try {
            taskParameters = getTaskParameters();

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }

        try {
            TaskProvider taskProvider = taskProviderFactory.getTaskProvider(
                storeID);
            String responseText = taskProvider.performTask(taskName,
                                                           taskParameters);

            return responseOk(msg, responseText);

        } catch (UnsupportedTaskException e) {
            return responseBad(msg, e, BAD_REQUEST);
        } catch (UnauthorizedException e) {
            return responseBad(msg, e, FORBIDDEN);
        } catch (StorageStateException e) {
            return responseBad(msg, e, CONFLICT);
        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    private String getTaskParameters() throws Exception {
        String taskParams = null;

        RestUtil.RequestContent content =
            restUtil.getRequestContent(request, headers);

        if(content != null) {
            InputStream contentStream = content.getContentStream();
            if(contentStream != null) {
                taskParams = IOUtil.readStringFromStream(contentStream);
            }
        }

        return taskParams;
    }

    private Response responseOk(String msg, String text) {
        log.debug(msg);
        return Response.ok(text, TEXT_PLAIN).build();
    }

    private Response responseOkXml(String msg, String text) {
        log.debug(msg);
        return Response.ok(text, APPLICATION_XML).build();
    }

    private Response responseBad(String msg,
                              Exception e,
                              Response.Status status) {
        log.error("Error: " + msg, e);
        String entity = e.getMessage() == null ? "null" : e.getMessage();
        return Response.status(status).entity(entity).build();
    }

}