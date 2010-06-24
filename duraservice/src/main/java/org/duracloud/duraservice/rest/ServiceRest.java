/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.error.DuraCloudCheckedException;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.duraservice.error.NoSuchDeployedServiceException;
import org.duracloud.duraservice.error.NoSuchServiceComputeInstanceException;
import org.duracloud.duraservice.error.NoSuchServiceException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Provides interaction with services via REST
 *
 * [POST /services] initialize DuraService
 * [GET /services (and /services?show=available) get list (XML) of all services available for deployment
 * [GET /services?show=deployed] get list (XML) of all deployed services
 * [GET /service/{serviceID}] get a particular service with all of its deployments
 * [GET /service/{serviceID}/{deploymentID}] gets a particular service with a particular deployment
 * [PUT /service/{serviceID}?serviceHost=[host-name] deploy a service
 * [POST /service/{serviceID}/{deploymentID}] update the configuration of a service deployment
 * [DELETE /service/{serviceID}/{deploymentID}] undeploy a service a deployed service
 *
 * @author Bill Branan
 */
@Path("/")
public class ServiceRest extends BaseRest {

    private static enum ServiceList {
        AVAILABLE ("available"),
        DEPLOYED ("deployed");

        public String type;

        ServiceList(String type) {
            this.type = type;
        }
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
    @Path("/services")
    @POST
    public Response initializeServices() {
        try {
            RestUtil restUtil = new RestUtil();
            RestUtil.RequestContent content = restUtil.getRequestContent(request, headers);
            ServiceResource.configureManager(content.getContentStream());
            String responseText = "Initialization Successful";
            return Response.ok(responseText, TEXT_PLAIN).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    /**
     * Gets a listing of services. Use the show parameter to specify which sets
     * should be included in the results:
     * show=available (default) - Include only the services available for deployment
     * show=deployed - Include only the services which have been deployed
     *
     * @param show determines which services list to retrieve (available or deployed)
     * @return 200 on success with a serialized list of services
     */
    @Path("/services")
    @GET
    @Produces(XML)
    public Response getServices(@QueryParam("show")
                                String show) {
        ResponseBuilder response = Response.ok();
        String serviceListXml = null;
        if(show == null ||
           show.equals("") ||
           show.equals(ServiceList.AVAILABLE.type)) {
            serviceListXml = ServiceResource.getAvailableServices();
        } else if(show.equals(ServiceList.DEPLOYED.type)) {
            serviceListXml = ServiceResource.getDeployedServices();
        } else {
            response = Response.serverError();
            response.entity("Invalid Request. Allowed values for show are " +
            		        "'available', and 'deployed'.");
            return response.build();
        }

        if(serviceListXml != null) {
            response.entity(serviceListXml);
        } else {
            response = Response.serverError();
            response.entity("Unable to retrieve services list.");
        }

        return response.build();
    }

    /**
     * Gets a full set of service information, including description,
     * configuration options, and a full listing of deployments. A service
     * does not have to be available for deployment in order to be retrieved
     *
     * @param serviceId the ID of the service to retrieve
     * @return 200 on success with a serialized service
     */
    @Path("/{serviceId}")
    @GET
    @Produces(XML)
    public Response getService(@PathParam("serviceId")
                               int serviceId) {
        ResponseBuilder response = Response.ok();
        String serviceXml;
        try {
            serviceXml = ServiceResource.getService(serviceId);
        } catch(NoSuchServiceException e) {
            return buildNotFoundResponse(e);
        }

        if(serviceXml != null) {
            response.entity(serviceXml);
        } else {
            response = Response.serverError();
            response.entity("Unable to retrieve service " + serviceId);
        }

        return response.build();
    }

    /**
     * Gets information pertaining to a deployed service.
     * Info includes description, configuration options, and a single
     * deployment, which includes configuration selections which are in use.
     *
     * @param serviceId the ID of the service to retrieve
     * @param deploymentId the ID of the deployment to retrieve
     * @return 200 on success with a serialized service
     */
    @Path("/{serviceId}/{deploymentId}")
    @GET
    @Produces(XML)
    public Response getDeployedService(@PathParam("serviceId")
                                       int serviceId,
                                       @PathParam("deploymentId")
                                       int deploymentId) {
        ResponseBuilder response = Response.ok();
        String serviceXml;
        try {
            serviceXml =
                ServiceResource.getDeployedService(serviceId, deploymentId);
        } catch(NoSuchDeployedServiceException e) {
            return buildNotFoundResponse(e);
        }

        if(serviceXml != null) {
            response.entity(serviceXml);
        } else {
            response = Response.serverError();
            response.entity("Unable to retrieve service " + serviceId +
                            " with deployment " + deploymentId);
        }

        return response.build();
    }

    /**
     * Gets the runtime properties of a deployed service.
     *
     * @param serviceId the ID of the service to retrieve
     * @param deploymentId the ID of the deployment to retrieve
     * @return 200 on success with serialized service properties
     */
    @Path("/{serviceId}/{deploymentId}/properties")
    @GET
    @Produces(XML)
    public Response getDeployedServiceProperties(@PathParam("serviceId")
                                                 int serviceId,
                                                 @PathParam("deploymentId")
                                                 int deploymentId) {
        ResponseBuilder response = Response.ok();
        String servicePropertiesXml;
        try {
            servicePropertiesXml =
                ServiceResource.getDeployedServiceProps(serviceId, deploymentId);
        } catch(NoSuchDeployedServiceException e) {
            return buildNotFoundResponse(e);
        }

        if(servicePropertiesXml != null) {
            response.entity(servicePropertiesXml);
        } else {
            response = Response.serverError();
            response.entity("Unable to retrieve properties for" +
                             " service " + serviceId +
                            " with deployment " + deploymentId);
        }

        return response.build();
    }

    /**
     * Deploys, Configures, and Starts a service.
     * It is expected that a call to get the configuration options
     * will be made prior to this call and selections/inputs will
     * be included as xml with this request.
     *
     * @param serviceId the ID of the service to deploy
     * @param serviceHost the server host on which to deploy the service
     * @return 201 on success with deploymentId of the new service deployment
     */
    @Path("/{serviceId}")
    @PUT
    public Response deployService(@PathParam("serviceId")
                                  int serviceId,
                                  @QueryParam("serviceHost")
                                  String serviceHost) {
        InputStream userConfigXml = getRequestContent();
        int deploymentId;
        try {
            deploymentId = ServiceResource.deployService(serviceId,
                                                         serviceHost,
                                                         userConfigXml);
        } catch(NoSuchServiceException e) {
            return buildNotFoundResponse(e);
        } catch(NoSuchServiceComputeInstanceException e) {
            return buildNotFoundResponse(e);
        }

        URI serviceUri = uriInfo.getRequestUri();
        String depServicePath = serviceUri.getPath() + "/" + deploymentId;
        URI deploymentUri = serviceUri;
        try {
            deploymentUri = new URI(serviceUri.getScheme(),
                                    serviceUri.getHost(),
                                    depServicePath,
                                    serviceUri.getFragment());
        } catch (URISyntaxException e){
            throw new RuntimeException(e);
        }
        return Response.created(deploymentUri).build();
    }

    /**
     * Re-Configures a deployed service.
     *
     * @param serviceId the ID of the service to reconfigure
     * @param deploymentId the ID of the deployment to reconfigure
     * @return 200 on success
     */
    @Path("/{serviceId}/{deploymentId}")
    @POST
    public Response configureService(@PathParam("serviceId")
                                     int serviceId,
                                     @PathParam("deploymentId")
                                     int deploymentId) {
        InputStream userConfigXml = getRequestContent();

        try {
            ServiceResource.updateServiceConfig(serviceId,
                                                deploymentId,
                                                userConfigXml);
        } catch(NoSuchDeployedServiceException e) {
            return buildNotFoundResponse(e);
        }
        return Response.ok().build();
    }

    /**
     * Stops and undeploys a service.
     *
     * @param serviceId the ID of the service to undeploy
     * @param deploymentId the ID of the deployment to undeploy
     * @return 200 on success
     */
    @Path("/{serviceId}/{deploymentId}")
    @DELETE
    public Response undeployService(@PathParam("serviceId")
                                    int serviceId,
                                    @PathParam("deploymentId")
                                    int deploymentId) {
        try {
            ServiceResource.undeployService(serviceId, deploymentId);
        } catch(NoSuchDeployedServiceException e) {
            return buildNotFoundResponse(e);
        }
        return Response.ok().build();
    }

    /*
     * Retrieves the content stream from an http request
     */
    private InputStream getRequestContent() {
        try {
            RestUtil restUtil = new RestUtil();
            RestUtil.RequestContent content =
                restUtil.getRequestContent(request, headers);
            return content.getContentStream();
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve request content");
        }
    }

    private Response buildNotFoundResponse(DuraCloudCheckedException e) {
        ResponseBuilder response = Response.status(HttpStatus.SC_NOT_FOUND);
        response.entity(e.getFormattedMessage());
        return response.build();
    }


}