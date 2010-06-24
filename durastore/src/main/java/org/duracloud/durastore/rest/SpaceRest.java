/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.durastore.error.ResourceException;
import org.duracloud.durastore.error.ResourceNotFoundException;
import org.duracloud.storage.error.InvalidIdException;

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

/**
 * Provides interaction with spaces via REST
 *
 * @author Bill Branan
 */
@Path("/")
public class SpaceRest extends BaseRest {

    /**
     * see SpaceResource.getSpaces()
     * @return 200 response with XML listing of spaces
     */
    @Path("/spaces")
    @GET
    @Produces(XML)
    public Response getSpaces(@QueryParam("storeID")
                              String storeID) {
        try {
            String xml = SpaceResource.getSpaces(storeID);
            return Response.ok(xml, TEXT_XML).build();
        } catch(ResourceException e) {
            return Response.serverError().entity(e.getMessage()).build();
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
        try {
            String xml = SpaceResource.getSpaceContents(spaceID,
                                                        storeID,
                                                        prefix,
                                                        maxResults,
                                                        marker);
            return addSpaceMetadataToResponse(Response.ok(xml, TEXT_XML),
                                              spaceID,
                                              storeID);
        } catch(ResourceNotFoundException e) {
            return Response.status(HttpStatus.SC_NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch(ResourceException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
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
        return addSpaceMetadataToResponse(Response.ok(), spaceID, storeID);
    }

    /**
     * Adds the metadata of a space as header values to the response
     */
    private Response addSpaceMetadataToResponse(ResponseBuilder response,
                                                String spaceID,
                                                String storeID) {
        try {
            Map<String, String> metadata =
                SpaceResource.getSpaceMetadata(spaceID, storeID);
            if(metadata != null) {
                Iterator<String> metadataNames = metadata.keySet().iterator();
                while(metadataNames.hasNext()) {
                    String metadataName = (String)metadataNames.next();
                    String metadataValue = metadata.get(metadataName);
                    response.header(HEADER_PREFIX + metadataName, metadataValue);
                }
            }
            return response.build();
        } catch(ResourceNotFoundException e) {
            return Response.status(HttpStatus.SC_NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch(ResourceException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
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
        try {            
            MultivaluedMap<String, String> rHeaders = headers.getRequestHeaders();

            String spaceAccess = "CLOSED";
            if(rHeaders.containsKey(SPACE_ACCESS_HEADER)) {
                spaceAccess = rHeaders.getFirst(SPACE_ACCESS_HEADER);
            }

            Map<String, String> userMetadata = getUserMetadata(SPACE_ACCESS_HEADER);
            SpaceResource.addSpace(spaceID,
                                   spaceAccess,
                                   userMetadata,
                                   storeID);
            URI location = uriInfo.getRequestUri();
            return Response.created(location).build();
        } catch(InvalidIdException e) {
            return Response.status(HttpStatus.SC_BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        } catch(ResourceException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
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
        try {
            MultivaluedMap<String, String> rHeaders = headers.getRequestHeaders();

            String spaceAccess = rHeaders.getFirst(SPACE_ACCESS_HEADER);

            Map<String, String> userMetadata = getUserMetadata(SPACE_ACCESS_HEADER);

            SpaceResource.updateSpaceMetadata(spaceID,
                                              spaceAccess,
                                              userMetadata,
                                              storeID);
            String responseText = "Space " + spaceID + " updated successfully";
            return Response.ok(responseText, TEXT_PLAIN).build();
        } catch(ResourceNotFoundException e) {
            return Response.status(HttpStatus.SC_NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch(ResourceException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
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
        try {
            SpaceResource.deleteSpace(spaceID, storeID);
            String responseText = "Space " + spaceID + " deleted successfully";
            return Response.ok(responseText, TEXT_PLAIN).build();
        } catch(ResourceNotFoundException e) {
            return Response.status(HttpStatus.SC_NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch(ResourceException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

}