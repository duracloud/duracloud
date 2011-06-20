/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.common.error.DuraCloudCheckedException;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.duraservice.error.NoSuchDeployedServiceException;
import org.duracloud.duraservice.error.NoSuchServiceComputeInstanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

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

    private final Logger log = LoggerFactory.getLogger(ServiceRest.class);

    private ServiceResource serviceResource;
    private RestUtil restUtil;

    public ServiceRest(ServiceResource serviceResource, RestUtil restUtil) {
        this.serviceResource = serviceResource;
        this.restUtil = restUtil;
    }

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
        String msg = "initializing services";

        try {
            RestUtil.RequestContent content = restUtil.getRequestContent(request, headers);
            serviceResource.configureManager(content.getContentStream());
            String responseText = "Initialization Successful";
            return responseOk(msg, responseText);

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
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
        String msg = "getting services(" + show + ")";

        try {
            return doGetServices(show);

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doGetServices(String show) {
        String msg = "doGetServices(" + show + ")";

        ResponseBuilder response = Response.ok();
        String serviceListXml = null;
        if(show == null ||
           show.equals("") ||
           show.equals(ServiceList.AVAILABLE.type)) {
            serviceListXml = serviceResource.getAvailableServices();
        } else if(show.equals(ServiceList.DEPLOYED.type)) {
            serviceListXml = serviceResource.getDeployedServices();
        } else {
            msg = "Invalid Request. Allowed values for show are 'available', " +
                "and 'deployed'.";
            log.error(msg);
            response = Response.serverError();
            response.entity(msg);
            return response.build();
        }

        if(serviceListXml != null) {
            response.entity(serviceListXml);
        } else {
            msg = "Unable to retrieve services list.";
            log.error(msg);
            response = Response.serverError();
            response.entity(msg);
            return response.build();
        }

        log.debug(msg);
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
        String msg = "getting service(" + serviceId + ")";

        try {
            log.debug(msg);
            return doGetService(serviceId);

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doGetService(int serviceId) {
        ResponseBuilder response = Response.ok();
        String serviceXml;
        try {
            serviceXml = serviceResource.getService(serviceId);
        } catch(NotFoundException e) {
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
        StringBuilder msg = new StringBuilder("getting deployed services(");
        msg.append(serviceId);
        msg.append(", ");
        msg.append(deploymentId);
        msg.append(")");

        try {
            log.debug(msg.toString());
            return doGetDeployedService(serviceId, deploymentId);

        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doGetDeployedService(int serviceId, int deploymentId) {
        ResponseBuilder response = Response.ok();
        String serviceXml;
        try {
            serviceXml =
                serviceResource.getDeployedService(serviceId, deploymentId);
        } catch(NotFoundException e) {
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
        StringBuilder msg = new StringBuilder("getting deployed services ");
        msg.append("properties(");
        msg.append(serviceId);
        msg.append(", ");
        msg.append(deploymentId);
        msg.append(")");

        try {
            log.debug(msg.toString());
            return doGetDeployedServiceProperties(serviceId, deploymentId);

        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doGetDeployedServiceProperties(int serviceId,
                                                    int deploymentId) {
        ResponseBuilder response = Response.ok();
        String servicePropertiesXml;
        try {
            servicePropertiesXml =
                serviceResource.getDeployedServiceProps(serviceId, deploymentId);
        } catch(NotFoundException e) {
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
        StringBuilder msg = new StringBuilder("deploying service(");
        msg.append(serviceId);
        msg.append(", ");
        msg.append(serviceHost);
        msg.append(")");

        try {
            log.debug(msg.toString());
            return doDeployService(serviceId, serviceHost);

        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doDeployService(int serviceId, String serviceHost)
        throws ServicesException {
        InputStream userConfigXml = getRequestContent();
        int deploymentId;
        try {
            deploymentId = serviceResource.deployService(serviceId,
                                                         serviceHost,
                                                         userConfigXml);
        } catch(NotFoundException e) {
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
                                     @PathParam("deploymentId") int deploymentId) {
        StringBuilder msg = new StringBuilder("configuring service(");
        msg.append(serviceId);
        msg.append(", ");
        msg.append(deploymentId);
        msg.append(")");

        try {
            log.debug(msg.toString());
            InputStream userConfigXml = getRequestContent();
            serviceResource.updateServiceConfig(serviceId,
                                                deploymentId,
                                                userConfigXml);

        } catch (NoSuchDeployedServiceException e) {
            return responseBad(msg.toString(), e, NOT_FOUND);

        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
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
        StringBuilder msg = new StringBuilder("undeploying service(");
        msg.append(serviceId);
        msg.append(", ");
        msg.append(deploymentId);
        msg.append(")");

        try {
            log.debug(msg.toString());
            serviceResource.undeployService(serviceId, deploymentId);

        } catch(NotFoundException e) {
            return responseBad(msg.toString(), e, NOT_FOUND);
            
        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
        }
        return Response.ok().build();
    }

    /*
     * Retrieves the content stream from an http request
     */
    private InputStream getRequestContent() {
        try {
            RestUtil.RequestContent content =
                restUtil.getRequestContent(request, headers);
            return content.getContentStream();
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve request content");
        }
    }

    private Response responseOk(String msg, String text) {
        log.debug(msg);
        return Response.ok(text, TEXT_PLAIN).build();
    }

    private Response responseBad(String msg,
                                 Exception e,
                                 Response.Status status) {
        log.error("Error: " + msg, e);
        String entity = e.getMessage() == null ? "null" : e.getMessage();
        return Response.status(status).entity(entity).build();
    }

    private Response buildNotFoundResponse(DuraCloudCheckedException e) {
        log.error(e.getFormattedMessage(), e);
        ResponseBuilder response = Response.status(HttpStatus.SC_NOT_FOUND);
        response.entity(e.getFormattedMessage());
        return response.build();
    }


}