/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_SPACE_ACL;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.duracloud.common.error.NoUserLoggedInException;
import org.duracloud.common.model.AclType;
import org.duracloud.common.model.Credential;
import org.duracloud.durastore.error.ResourceException;
import org.duracloud.durastore.error.ResourceNotFoundException;
import org.duracloud.security.context.SecurityContextUtil;
import org.duracloud.storage.error.InvalidIdException;
import org.duracloud.storage.error.SpaceAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides interaction with spaces via REST
 *
 * @author Bill Branan
 */
@Path("/")
@Component
public class SpaceRest extends BaseRest {
    private final Logger log = LoggerFactory.getLogger(SpaceRest.class);

    private SpaceResource spaceResource;
    private SecurityContextUtil securityContextUtil;

    @Autowired
    public SpaceRest(SpaceResource spaceResource,
                     SecurityContextUtil securityContextUtil) {
        this.spaceResource = spaceResource;
        this.securityContextUtil = securityContextUtil;
    }

    /**
     * see SpaceResource.getSpaces()
     *
     * @return 200 response with XML listing of spaces
     */
    @Path("/spaces")
    @GET
    @Produces(XML)
    public Response getSpaces(@QueryParam("storeID") String storeID) {
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
     * see SpaceResource.getSpaceProperties(String, String);
     * see SpaceResource.getSpaceContents(String, String);
     *
     * @return 200 response with XML listing of space content and
     * space properties included as header values
     */
    @Path("/{spaceID}")
    @GET
    @Produces(XML)
    public Response getSpace(@PathParam("spaceID") String spaceID,
                             @QueryParam("storeID") String storeID,
                             @QueryParam("prefix") String prefix,
                             @QueryParam("maxResults") long maxResults,
                             @QueryParam("marker") String marker) {
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

        } catch (ResourceNotFoundException e) {
            return responseNotFound(msg.toString(), e, NOT_FOUND);

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
        return addSpacePropertiesToResponse(Response.ok(xml, APPLICATION_XML),
                                            spaceID,
                                            storeID);
    }

    /**
     * see SpaceResource.getSpaceProperties(String, String);
     *
     * @return 200 response with space properties included as header values
     */
    @Path("/{spaceID}")
    @HEAD
    public Response getSpaceProperties(@PathParam("spaceID") String spaceID,
                                       @QueryParam("storeID") String storeID) {
        String msg = "adding space properties(" + spaceID + ", " + storeID + ")";

        try {
            log.debug(msg);
            return addSpacePropertiesToResponse(Response.ok(), spaceID, storeID);

        } catch (ResourceNotFoundException e) {
            return responseNotFound(msg, e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds the properties of a space as header values to the response
     */
    private Response addSpacePropertiesToResponse(ResponseBuilder response,
                                                  String spaceID,
                                                  String storeID)
        throws ResourceException {
        Map<String, String> properties =
            spaceResource.getSpaceProperties(spaceID, storeID);

        return addPropertiesToResponse(response, properties);
    }

    private Response addPropertiesToResponse(ResponseBuilder response,
                                             Map<String, String> properties) {
        for (String propName : properties.keySet()) {
            response.header(HEADER_PREFIX + propName, properties.get(propName));
        }
        return response.build();
    }

    /**
     * see SpaceResource.getSpaceACLs(String, String);
     *
     * @return 200 response with space ACLs included as header values
     */
    @Path("/acl/{spaceID}")
    @HEAD
    public Response getSpaceACLs(@PathParam("spaceID") String spaceID,
                                 @QueryParam("storeID") String storeID) {
        String msg = "getting space ACLs(" + spaceID + ", " + storeID + ")";

        try {
            log.debug(msg);
            return addSpaceACLsToResponse(Response.ok(), spaceID, storeID);

        } catch (ResourceNotFoundException e) {
            return responseNotFound(msg, e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds the ACLs of a space as header values to the response.
     */
    private Response addSpaceACLsToResponse(ResponseBuilder response,
                                            String spaceID,
                                            String storeID)
        throws ResourceException {
        Map<String, String> aclProps = new HashMap<String, String>();
        Map<String, AclType> acls = spaceResource.getSpaceACLs(spaceID,
                                                               storeID);
        for (String key : acls.keySet()) {
            aclProps.put(key, acls.get(key).name());
        }
        return addPropertiesToResponse(response, aclProps);
    }

    /**
     * see SpaceResource.addSpace(String, String, String, String)
     *
     * @return 201 response with request URI
     */
    @Path("/{spaceID}")
    @PUT
    public Response addSpace(@PathParam("spaceID") String spaceID,
                             @QueryParam("storeID") String storeID) {
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
        Map<String, AclType> userACLs = getUserACLs();

        try {
            spaceResource.addSpace(spaceID,
                                   userACLs,
                                   storeID);
        } catch (SpaceAlreadyExistsException e) {
            log.info("Create space called on " + spaceID +
                     " but space already exists. Space setup skipped.");
        }
        URI location = uriInfo.getRequestUri();
        return Response.created(location).build();
    }

    private Map<String, AclType> getUserACLs() {
        Map<String, AclType> acls = new HashMap<String, AclType>();
        try {
            Credential currentUser = securityContextUtil.getCurrentUser();
            acls.put(PROPERTIES_SPACE_ACL + currentUser.getUsername(),
                     AclType.WRITE);

        } catch (NoUserLoggedInException e) {
            log.warn("Adding user acl, error: {}", e);
        }
        return acls;
    }

    /**
     * This method sets the ACLs associated with a space.
     * Only values included in the ACLs headers will be updated, others will
     * be removed.
     *
     * @return 200 response
     */
    @Path("/acl/{spaceID}")
    @POST
    public Response updateSpaceACLs(@PathParam("spaceID") String spaceID,
                                    @QueryParam("storeID") String storeID) {
        String msg = "update space ACLs(" + spaceID + ", " + storeID + ")";

        try {
            log.debug(msg);
            return doUpdateSpaceACLs(spaceID, storeID);

        } catch (ResourceNotFoundException e) {
            return responseNotFound(msg, e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg, e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doUpdateSpaceACLs(String spaceID, String storeID)
        throws ResourceException {
        Map<String, AclType> spaceACLs = getSpaceACLs();
        spaceResource.updateSpaceACLs(spaceID, spaceACLs, storeID);

        String responseText = "Space " + spaceID + " ACLs updated successfully";
        return Response.ok(responseText, TEXT_PLAIN).build();
    }

    /**
     * see SpaceResource.deleteSpace(String, String);
     *
     * @return 200 response indicating space deleted successfully
     */
    @Path("/{spaceID}")
    @DELETE
    public Response deleteSpace(@PathParam("spaceID") String spaceID,
                                @QueryParam("storeID") String storeID) {
        String msg = "deleting space(" + spaceID + ", " + storeID + ")";

        try {
            log.debug(msg);
            return doDeleteSpace(spaceID, storeID);

        } catch (ResourceNotFoundException e) {
            return responseNotFound(msg, e, NOT_FOUND);

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

    private Response responseNotFound(String msg,
                                      Exception e,
                                      Response.Status status) {
        log.debug("Not Found: " + msg);
        return responseBad(e.getMessage(), status);
    }

    private Response responseBad(String msg,
                                 Exception e,
                                 Response.Status status) {
        log.error("Error: " + msg, e);
        return responseBad(e.getMessage(), status);
    }

}