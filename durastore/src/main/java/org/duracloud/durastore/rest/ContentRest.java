/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.rest.HttpHeaders;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.durastore.error.ResourceException;
import org.duracloud.durastore.error.ResourceNotFoundException;
import org.duracloud.storage.error.InvalidIdException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Provides interaction with content via REST
 *
 * @author Bill Branan
 */
@Path("/{spaceID}/{contentID: [^?]+}")
public class ContentRest extends BaseRest {
    private final Logger log = LoggerFactory.getLogger(ContentRest.class);

    private ContentResource contentResource;
    private RestUtil restUtil;

    public ContentRest(ContentResource contentResource, RestUtil restUtil) {
        this.contentResource = contentResource;
        this.restUtil = restUtil;
    }

    /**
     * see ContentResource.getContent()
     * see ContentResource.getContentProperties()
     * @return 200 response with content stream as body and content properties as headers
     */
    @GET
    public Response getContent(@PathParam("spaceID")
                               String spaceID,
                               @PathParam("contentID")
                               String contentID,
                               @QueryParam("storeID")
                               String storeID, 
                               @QueryParam("attachment")
                               boolean attachment) {
        StringBuilder msg = new StringBuilder("getting content(");
        msg.append(spaceID);
        msg.append(", ");
        msg.append(contentID);
        msg.append(", ");
        msg.append(storeID);
        msg.append(", ");
        msg.append(attachment);
        msg.append(")");

        try {
            log.debug(msg.toString());
            return doGetContent(spaceID, contentID, storeID, attachment);

        } catch (ResourceNotFoundException e) {
            return responseBad(msg.toString(), e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doGetContent(String spaceID,
                                  String contentID,
                                  String storeID,
                                  boolean attachment) throws ResourceException {
        Map<String, String> properties =
            contentResource.getContentProperties(spaceID, contentID, storeID);
        InputStream content =
            contentResource.getContent(spaceID, contentID, storeID);

        ResponseBuilder responseBuilder = Response.ok(content);

        if(attachment){
            addContentDispositionHeader(responseBuilder, contentID);
        }
        return addContentPropertiesToResponse(responseBuilder,
                                              properties);
    }

    private void addContentDispositionHeader(ResponseBuilder responseBuilder,
                                             String filename) {
        StringBuffer contentDisposition = new StringBuffer();
        contentDisposition.append("attachment;");
        contentDisposition.append("filename=\"");
        contentDisposition.append(filename);
        contentDisposition.append("\"");
        responseBuilder.header(HttpHeaders.CONTENT_DISPOSITION,
                               contentDisposition.toString());
    }

    /**
     * see ContentResource.getContentProperties()
     * @return 200 response with content properties as headers
     */
    @HEAD
    public Response getContentProperties(@PathParam("spaceID")
                                         String spaceID,
                                         @PathParam("contentID")
                                         String contentID,
                                         @QueryParam("storeID")
                                         String storeID) {
        StringBuilder msg = new StringBuilder("getting content properties(");
        msg.append(spaceID);
        msg.append(", ");
        msg.append(contentID);
        msg.append(", ");
        msg.append(storeID);
        msg.append(")");

        try {
            Map<String, String> properties =
                contentResource.getContentProperties(spaceID, contentID, storeID);

            log.debug(msg.toString());
            return addContentPropertiesToResponse(Response.ok(), properties);

        } catch (ResourceNotFoundException e) {
            return responseBad(msg.toString(), e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds the properties of a content item as header values to the response.
     * See http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1
     * for specifics on particular headers.
     */
    private Response addContentPropertiesToResponse(ResponseBuilder response,
                                                    Map<String, String> properties) {
        if(properties != null) {
            // Flags that, when set to true, indicate that the
            // authoritative value for this data has already
            // been set and should not be overwritten
            boolean contentTypeSet = false;
            boolean contentSizeSet = false;
            boolean contentChecksumSet = false;
            boolean contentModifiedSet = false;

            Iterator<String> propertiesNames = properties.keySet().iterator();
            while(propertiesNames.hasNext()) {
                String propertiesName = (String)propertiesNames.next();
                String propertiesValue = properties.get(propertiesName);

                if(propertiesName.equals(StorageProvider.PROPERTIES_CONTENT_MIMETYPE)) {
                    if(validMimetype(propertiesValue)) {
                        response.header(HttpHeaders.CONTENT_TYPE, propertiesValue);
                        contentTypeSet = true;
                    } else {
                        response.header(HttpHeaders.CONTENT_TYPE, DEFAULT_MIME);
                    }
                } else if(propertiesName.equals(StorageProvider.PROPERTIES_CONTENT_SIZE)) {
                    response.header(HttpHeaders.CONTENT_LENGTH, propertiesValue);
                    contentSizeSet = true;
                } else if(propertiesName.equals(StorageProvider.PROPERTIES_CONTENT_CHECKSUM)) {
                    response.header(HttpHeaders.CONTENT_MD5, propertiesValue);
                    response.header(HttpHeaders.ETAG, propertiesValue);
                    contentChecksumSet = true;
                } else if(propertiesName.equals(StorageProvider.PROPERTIES_CONTENT_MODIFIED)) {
                    response.header(HttpHeaders.LAST_MODIFIED, propertiesValue);
                    contentModifiedSet = true;
                } else if((propertiesName.equals(HttpHeaders.CONTENT_TYPE))) {
                    if(!contentTypeSet) {
                        if(validMimetype(propertiesValue)) {
                            response.header(HttpHeaders.CONTENT_TYPE, propertiesValue);
                        } else {
                            response.header(HttpHeaders.CONTENT_TYPE, DEFAULT_MIME);
                        }
                    }
                } else if(propertiesName.equals(HttpHeaders.CONTENT_LENGTH)) {
                    if(!contentSizeSet) {
                        response.header(propertiesName, propertiesValue);
                    }
                } else if(propertiesName.equalsIgnoreCase(HttpHeaders.CONTENT_MD5)) {
                    if(!contentChecksumSet) {
                        response.header(HttpHeaders.CONTENT_MD5, propertiesValue);
                    }
                } else if(propertiesName.equals(HttpHeaders.ETAG)) {
                    if(!contentChecksumSet) {
                        response.header(propertiesName, propertiesValue);
                    }
                } else if(propertiesName.equals(HttpHeaders.LAST_MODIFIED)) {
                    if(!contentModifiedSet) {
                        response.header(propertiesName, propertiesValue);
                    }
                } else if(propertiesName.equals(HttpHeaders.DATE) ||
                          propertiesName.equals(HttpHeaders.CONNECTION)) {
                    // Ignore this value
                } else if(propertiesName.equals(HttpHeaders.AGE) ||
                          propertiesName.equals(HttpHeaders.CACHE_CONTROL) ||
                          propertiesName.equals(HttpHeaders.CONTENT_ENCODING) ||
                          propertiesName.equals(HttpHeaders.CONTENT_LANGUAGE) ||
                          propertiesName.equals(HttpHeaders.CONTENT_LOCATION) ||
                          propertiesName.equals(HttpHeaders.CONTENT_RANGE) ||
                          propertiesName.equals(HttpHeaders.EXPIRES) ||
                          propertiesName.equals(HttpHeaders.LOCATION) ||
                          propertiesName.equals(HttpHeaders.PRAGMA) ||
                          propertiesName.equals(HttpHeaders.RETRY_AFTER) ||
                          propertiesName.equals(HttpHeaders.SERVER) ||
                          propertiesName.equals(HttpHeaders.TRANSFER_ENCODING) ||
                          propertiesName.equals(HttpHeaders.UPGRADE) ||
                          propertiesName.equals(HttpHeaders.WARNING)) {
                    // Pass through as a standard http header
                    response.header(propertiesName, propertiesValue);
                } else {
                    // Custom header, append prefix
                    response.header(HEADER_PREFIX + propertiesName, propertiesValue);
                }
            }
        }
        return response.build();
    }

    protected boolean validMimetype(String mimetype) {
        boolean valid = true;
        if(mimetype == null || mimetype.equals("")) {
            valid = false;
        } else {
            try {
                MediaType.valueOf(mimetype);
            } catch(IllegalArgumentException e) {
                valid = false;
            }
        }
        return valid;
    }

    /**
     * see ContentResource.updateContentProperties()
     * @return 200 response indicating content properties updated successfully
     */
    @POST
    public Response updateContentProperties(@PathParam("spaceID")
                                            String spaceID,
                                            @PathParam("contentID")
                                            String contentID,
                                            @QueryParam("storeID")
                                            String storeID) {
        StringBuilder msg = new StringBuilder("updating content properties(");
        msg.append(spaceID);
        msg.append(", ");
        msg.append(contentID);
        msg.append(", ");
        msg.append(storeID);
        msg.append(")");

        try {
            log.debug(msg.toString());
            return doUpdateContentProperties(spaceID, contentID, storeID);

        } catch (ResourceNotFoundException e) {
            return responseBad(msg.toString(), e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doUpdateContentProperties(String spaceID,
                                               String contentID,
                                               String storeID)
        throws ResourceException {
        MultivaluedMap<String, String> rHeaders =
            headers.getRequestHeaders();
        Map<String, String> userProperties =
            getUserProperties(CONTENT_MIMETYPE_HEADER);

        // Set mimetype in properties if it was provided
        String contentMimeType = null;
        if(rHeaders.containsKey(CONTENT_MIMETYPE_HEADER)) {
            contentMimeType = rHeaders.getFirst(CONTENT_MIMETYPE_HEADER);
        }
        if(contentMimeType == null && rHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
            contentMimeType = rHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
        }
        if(contentMimeType != null && !contentMimeType.equals("")) {
            if(validMimetype(contentMimeType)) {
                userProperties.put(StorageProvider.PROPERTIES_CONTENT_MIMETYPE,
                                 contentMimeType);
            } else {
                throw new ResourceException("Invalid Mimetype");
            }
        }

        contentResource.updateContentProperties(spaceID,
                                                contentID,
                                                contentMimeType,
                                                userProperties,
                                                storeID);
        String responseText = "Content " + contentID + " updated successfully";
        return Response.ok(responseText, TEXT_PLAIN).build();
    }

    /**
     * see ContentResource.addContent()
     * @return 201 response indicating content added successfully
     */
    @PUT
    public Response addContent(@PathParam("spaceID")
                               String spaceID,
                               @PathParam("contentID")
                               String contentID,
                               @QueryParam("storeID")
                               String storeID) {
        StringBuilder msg = new StringBuilder("adding content(");
        msg.append(spaceID);
        msg.append(", ");
        msg.append(contentID);
        msg.append(", ");
        msg.append(storeID);
        msg.append(")");

        try {
            log.debug(msg.toString());
            return doAddContent(spaceID, contentID, storeID);

        } catch (InvalidIdException e) {
            return responseBad(msg.toString(), e, BAD_REQUEST);

        } catch (ResourceNotFoundException e) {
            return responseBad(msg.toString(), e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doAddContent(String spaceID,
                                  String contentID,
                                  String storeID) throws Exception {
        RestUtil.RequestContent content = restUtil.getRequestContent(request,
                                                                     headers);

        String checksum = null;
        MultivaluedMap<String, String> rHeaders = headers.getRequestHeaders();
        if(rHeaders.containsKey(HttpHeaders.CONTENT_MD5)) {
            checksum = rHeaders.getFirst(HttpHeaders.CONTENT_MD5);
        }

        if (content != null) {
            checksum = contentResource.addContent(spaceID,
                                                  contentID,
                                                  content.getContentStream(),
                                                  content.getMimeType(),
                                                  content.getSize(),
                                                  checksum,
                                                  storeID);
            updateContentProperties(spaceID, contentID, storeID);
            URI location = uriInfo.getRequestUri();
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(StorageProvider.PROPERTIES_CONTENT_CHECKSUM, checksum);
            return addContentPropertiesToResponse(Response.created(location),
                                                properties);

        } else {
            String error = "Content could not be retrieved from the request.";
            log.error(error);
            return Response.status(HttpStatus.SC_BAD_REQUEST)
                .entity(error)
                .build();
        }
    }

    /**
     * see ContentResource.removeContent()
     * @return 200 response indicating content removed successfully
     */
    @DELETE
    public Response deleteContent(@PathParam("spaceID")
                                  String spaceID,
                                  @PathParam("contentID")
                                  String contentID,
                                  @QueryParam("storeID")
                                  String storeID) {
        StringBuilder msg = new StringBuilder("deleting content(");
        msg.append(spaceID);
        msg.append(", ");
        msg.append(contentID);
        msg.append(", ");
        msg.append(storeID);
        msg.append(")");

        try {
            contentResource.deleteContent(spaceID, contentID, storeID);
            String responseText = "Content " + contentID + " deleted successfully";
            return responseOk(msg.toString(), responseText);

        } catch(ResourceNotFoundException e) {
            return responseBad(msg.toString(), e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
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

}