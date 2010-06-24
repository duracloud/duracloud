/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.durastore.util.TaskProviderFactory;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.duracloud.storage.provider.TaskProvider;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

/**
 * Allows for calling storage provider specific tasks
 *
 * @author Bill Branan
 *         Date: May 20, 2010
 */
@Path("/task")
public class TaskRest extends BaseRest {

    /**
     * Gets a listing of supported tasks for a given provider
     *
     * @return 200 on success
     */
    @GET
    public Response getSupportedTasks(@QueryParam("storeID")
                                      String storeID){
        try {
            TaskProvider taskProvider =
                TaskProviderFactory.getTaskProvider(storeID);

            List<String> supportedTasks = taskProvider.getSupportedTasks();
            String responseText =
                SerializationUtil.serializeList(supportedTasks);

            return Response.ok(responseText, TEXT_PLAIN).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
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
        String taskParameters = null;
        try {
            taskParameters = getTaskParameters();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }

        try {
            TaskProvider taskProvider =
                TaskProviderFactory.getTaskProvider(storeID);
            String responseText =
                taskProvider.performTask(taskName, taskParameters);
            return Response.ok(responseText, TEXT_PLAIN).build();
        } catch (UnsupportedTaskException e) {
            return Response.status(HttpStatus.SC_BAD_REQUEST).
                   entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private String getTaskParameters() throws Exception {
        String taskParams = null;

        RestUtil restUtil = new RestUtil();
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

}