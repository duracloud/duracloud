/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.durastore.error.ResourceException;
import org.duracloud.durastore.error.ResourceNotFoundException;
import org.duracloud.storage.error.InvalidIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Provides interaction with spaces via REST
 *
 * @author Bill Branan
 */
@Path("/")
public class SpaceRest extends BaseRest {
    private final Logger log = LoggerFactory.getLogger(SpaceRest.class);

    private SpaceResource spaceResource;

    public SpaceRest(SpaceResource spaceResource) {
        this.spaceResource = spaceResource;
    }

    /**
     * see SpaceResource.getSpaces()
     * @return 200 response with XML listing of spaces
     */
    @Path("/spaces")
    @GET
    @Produces(XML)
    public Response getSpaces(@QueryParam("storeID")
                              String storeID) {
        String msg = "getting spaces(" + storeID + ")";

        try {
            String xml = spaceResource.getSpaces(storeID);
            return responseOkXml(msg, xml);

        } catch (ResourceException e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * see SpaceResource.getSpaceMetadata(String, String);
     * see SpaceResource.getSpaceContents(String, String);
     * @return 200 response with XML listing of space content and
     *         space metadata included as header values
     */
    @Path("/{spaceID}")
    @GET
    @Produces(XML)
    public Response getSpace(@PathParam("spaceID")
                             String spaceID,
                             @QueryParam("storeID")
                             String storeID,
                             @QueryParam("prefix")
                             String prefix,
                             @QueryParam("maxResults")
                             long maxResults,
                             @QueryParam("marker")
                             String marker) {
        StringBuilder msg = new StringBuilder("getting space contents(");
        msg.append(spaceID);
        msg.append(", ");
        msg.append(storeID);
        msg.append(", ");
        msg.append(prefix);
        msg.append(", ");
        msg.append(maxResults);
        msg.append(", ");
        msg.append(marker);
        msg.append(")");

        try {
            log.debug(msg.toString());
            return doGetSpace(spaceID, storeID, prefix, maxResults, marker);

        } catch(ResourceNotFoundException e) {
            return responseBad(msg.toString(), e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
            
        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doGetSpace(String spaceID,
                                String storeID,
                                String prefix,
                                long maxResults,
                                String marker) throws ResourceException {
        String xml = spaceResource.getSpaceContents(spaceID,
                                                    storeID,
                                                    prefix,
                                                    maxResults,
                                                    marker);
        return addSpaceMetadataToResponse(Response.ok(xml, APPLICATION_XML),
                                          spaceID,
                                          storeID);
    }

    /**
     * see SpaceResource.getSpaceMetadata(String, String);
     * @return 200 response with space metadata included as header values
     */
    @Path("/{spaceID}")
    @HEAD
    public Response getSpaceMetadata(@PathParam("spaceID")
                                     String spaceID,
                                     @QueryParam("storeID")
                                     String storeID){
        String msg = "adding space metadata(" + spaceID + ", " + storeID + ")";

        try {
            log.debug(msg);
            return addSpaceMetadataToResponse(Response.ok(), spaceID, storeID);

        } catch (ResourceNotFoundException e) {
            return responseBad(msg, e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds the metadata of a space as header values to the response
     */
    private Response addSpaceMetadataToResponse(ResponseBuilder response,
                                                String spaceID,
                                                String storeID)
        throws ResourceException {
        Map<String, String> metadata = spaceResource.getSpaceMetadata(spaceID,
                                                                      storeID);
        if (metadata != null) {
            Iterator<String> metadataNames = metadata.keySet().iterator();
            while (metadataNames.hasNext()) {
                String metadataName = (String) metadataNames.next();
                String metadataValue = metadata.get(metadataName);
                response.header(HEADER_PREFIX + metadataName, metadataValue);
            }
        }
        return response.build();
    }

    /**
     * see SpaceResource.addSpace(String, String, String, String)
     * @return 201 response with request URI
     */
    @Path("/{spaceID}")
    @PUT
    public Response addSpace(@PathParam("spaceID")
                             String spaceID,
                             @QueryParam("storeID")
                             String storeID){
        String msg = "adding space(" + spaceID + ", " + storeID + ")";

        try {
            log.debug(msg);
            return doAddSpace(spaceID, storeID);

        } catch (InvalidIdException e) {
            return responseBad(msg, e, BAD_REQUEST);

        } catch (ResourceException e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doAddSpace(String spaceID, String storeID)
        throws ResourceException, InvalidIdException {
        MultivaluedMap<String, String> rHeaders = headers.getRequestHeaders();

        String spaceAccess = "CLOSED";
        if(rHeaders.containsKey(SPACE_ACCESS_HEADER)) {
            spaceAccess = rHeaders.getFirst(SPACE_ACCESS_HEADER);
        }

        Map<String, String> userMetadata = getUserMetadata(SPACE_ACCESS_HEADER);
        spaceResource.addSpace(spaceID,
                               spaceAccess,
                               userMetadata,
                               storeID);
        URI location = uriInfo.getRequestUri();
        return Response.created(location).build();
    }

    /**
     * see SpaceResource.updateSpaceMetadata(String, String, String, String);
     * @return 200 response with XML listing of space metadata
     */
    @Path("/{spaceID}")
    @POST
    public Response updateSpaceMetadata(@PathParam("spaceID")
                                        String spaceID,
                                        @QueryParam("storeID")
                                        String storeID){
        String msg = "update space metadata(" + spaceID + ", " + storeID + ")";

        try {
            log.debug(msg);
            return doUpdateSpaceMetadata(spaceID, storeID);

        } catch (ResourceNotFoundException e) {
            return responseBad(msg, e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doUpdateSpaceMetadata(String spaceID, String storeID)
        throws ResourceException {
        MultivaluedMap<String, String> rHeaders = headers.getRequestHeaders();

        String spaceAccess = rHeaders.getFirst(SPACE_ACCESS_HEADER);

        Map<String, String> userMetadata = getUserMetadata(SPACE_ACCESS_HEADER);

        spaceResource.updateSpaceMetadata(spaceID,
                                          spaceAccess,
                                          userMetadata,
                                          storeID);
        String responseText = "Space " + spaceID + " updated successfully";
        return Response.ok(responseText, TEXT_PLAIN).build();
    }

    /**
     * see SpaceResource.deleteSpace(String, String);
     * @return 200 response indicating space deleted successfully
     */
    @Path("/{spaceID}")
    @DELETE
    public Response deleteSpace(@PathParam("spaceID")
                                String spaceID,
                                @QueryParam("storeID")
                                String storeID){
        String msg = "deleting space(" + spaceID + ", " + storeID + ")";

        try {
            log.debug(msg);
            return doDeleteSpace(spaceID, storeID);

        } catch (ResourceNotFoundException e) {
            return responseBad(msg, e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doDeleteSpace(String spaceID, String storeID)
        throws ResourceException {
        spaceResource.deleteSpace(spaceID, storeID);
        String responseText = "Space " + spaceID + " deleted successfully";
        return Response.ok(responseText, TEXT_PLAIN).build();
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