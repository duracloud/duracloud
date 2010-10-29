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
     * see ContentResource.getContentMetadata()
     * @return 200 response with content stream as body and content metadata as headers
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
        Map<String, String> metadata =
            contentResource.getContentMetadata(spaceID, contentID, storeID);
        InputStream content =
            contentResource.getContent(spaceID, contentID, storeID);

        ResponseBuilder responseBuilder = Response.ok(content);

        if(attachment){
            addContentDispositionHeader(responseBuilder, contentID);
        }
        return addContentMetadataToResponse(responseBuilder,
                                            metadata);
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
     * see ContentResource.getContentMetadata()
     * @return 200 response with content metadata as headers
     */
    @HEAD
    public Response getContentMetadata(@PathParam("spaceID")
                                       String spaceID,
                                       @PathParam("contentID")
                                       String contentID,
                                       @QueryParam("storeID")
                                       String storeID) {
        StringBuilder msg = new StringBuilder("getting content metadata(");
        msg.append(spaceID);
        msg.append(", ");
        msg.append(contentID);
        msg.append(", ");
        msg.append(storeID);
        msg.append(")");

        try {
            Map<String, String> metadata =
                contentResource.getContentMetadata(spaceID, contentID, storeID);

            log.debug(msg.toString());
            return addContentMetadataToResponse(Response.ok(), metadata);

        } catch (ResourceNotFoundException e) {
            return responseBad(msg.toString(), e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds the metadata of a content item as header values to the response.
     * See http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1
     * for specifics on particular headers.
     */
    private Response addContentMetadataToResponse(ResponseBuilder response,
                                                  Map<String, String> metadata) {
        if(metadata != null) {
            // Flags that, when set to true, indicate that the
            // authoritative value for this data has already
            // been set and should not be overwritten
            boolean contentTypeSet = false;
            boolean contentSizeSet = false;
            boolean contentChecksumSet = false;
            boolean contentModifiedSet = false;

            Iterator<String> metadataNames = metadata.keySet().iterator();
            while(metadataNames.hasNext()) {
                String metadataName = (String)metadataNames.next();
                String metadataValue = metadata.get(metadataName);

                if(metadataName.equals(StorageProvider.METADATA_CONTENT_MIMETYPE)) {
                    if(validMimetype(metadataValue)) {
                        response.header(HttpHeaders.CONTENT_TYPE, metadataValue);
                        contentTypeSet = true;
                    } else {
                        response.header(HttpHeaders.CONTENT_TYPE, DEFAULT_MIME);
                    }
                } else if(metadataName.equals(StorageProvider.METADATA_CONTENT_SIZE)) {
                    response.header(HttpHeaders.CONTENT_LENGTH, metadataValue);
                    contentSizeSet = true;
                } else if(metadataName.equals(StorageProvider.METADATA_CONTENT_CHECKSUM)) {
                    response.header(HttpHeaders.CONTENT_MD5, metadataValue);
                    response.header(HttpHeaders.ETAG, metadataValue);
                    contentChecksumSet = true;
                } else if(metadataName.equals(StorageProvider.METADATA_CONTENT_MODIFIED)) {
                    response.header(HttpHeaders.LAST_MODIFIED, metadataValue);
                    contentModifiedSet = true;
                } else if((metadataName.equals(HttpHeaders.CONTENT_TYPE))) {
                    if(!contentTypeSet) {
                        if(validMimetype(metadataValue)) {
                            response.header(HttpHeaders.CONTENT_TYPE, metadataValue);
                        } else {
                            response.header(HttpHeaders.CONTENT_TYPE, DEFAULT_MIME);
                        }
                    }
                } else if(metadataName.equals(HttpHeaders.CONTENT_LENGTH)) {
                    if(!contentSizeSet) {
                        response.header(metadataName, metadataValue);
                    }
                } else if(metadataName.equalsIgnoreCase(HttpHeaders.CONTENT_MD5)) {
                    if(!contentChecksumSet) {
                        response.header(HttpHeaders.CONTENT_MD5, metadataValue);
                    }
                } else if(metadataName.equals(HttpHeaders.ETAG)) {
                    if(!contentChecksumSet) {
                        response.header(metadataName, metadataValue);
                    }
                } else if(metadataName.equals(HttpHeaders.LAST_MODIFIED)) {
                    if(!contentModifiedSet) {
                        response.header(metadataName, metadataValue);
                    }
                } else if(metadataName.equals(HttpHeaders.DATE) ||
                          metadataName.equals(HttpHeaders.CONNECTION)) {
                    // Ignore this value
                } else if(metadataName.equals(HttpHeaders.AGE) ||
                          metadataName.equals(HttpHeaders.CACHE_CONTROL) ||
                          metadataName.equals(HttpHeaders.CONTENT_ENCODING) ||
                          metadataName.equals(HttpHeaders.CONTENT_LANGUAGE) ||
                          metadataName.equals(HttpHeaders.CONTENT_LOCATION) ||
                          metadataName.equals(HttpHeaders.CONTENT_RANGE) ||
                          metadataName.equals(HttpHeaders.EXPIRES) ||
                          metadataName.equals(HttpHeaders.LOCATION) ||
                          metadataName.equals(HttpHeaders.PRAGMA) ||
                          metadataName.equals(HttpHeaders.RETRY_AFTER) ||
                          metadataName.equals(HttpHeaders.SERVER) ||
                          metadataName.equals(HttpHeaders.TRANSFER_ENCODING) ||
                          metadataName.equals(HttpHeaders.UPGRADE) ||
                          metadataName.equals(HttpHeaders.WARNING)) {
                    // Pass through as a standard http header
                    response.header(metadataName, metadataValue);
                } else {
                    // Custom header, append prefix
                    response.header(HEADER_PREFIX + metadataName, metadataValue);
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
     * see ContentResource.updateContentMetadata()
     * @return 200 response indicating content metadata updated successfully
     */
    @POST
    public Response updateContentMetadata(@PathParam("spaceID")
                                          String spaceID,
                                          @PathParam("contentID")
                                          String contentID,
                                          @QueryParam("storeID")
                                          String storeID) {
        StringBuilder msg = new StringBuilder("updating content metadata(");
        msg.append(spaceID);
        msg.append(", ");
        msg.append(contentID);
        msg.append(", ");
        msg.append(storeID);
        msg.append(")");

        try {
            log.debug(msg.toString());
            return doUpdateContentMetadata(spaceID, contentID, storeID);

        } catch (ResourceNotFoundException e) {
            return responseBad(msg.toString(), e, NOT_FOUND);

        } catch (ResourceException e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doUpdateContentMetadata(String spaceID,
                                             String contentID,
                                             String storeID)
        throws ResourceException {
        MultivaluedMap<String, String> rHeaders =
            headers.getRequestHeaders();
        Map<String, String> userMetadata =
            getUserMetadata(CONTENT_MIMETYPE_HEADER);

        // Set mimetype in metadata if it was provided
        String contentMimeType = null;
        if(rHeaders.containsKey(CONTENT_MIMETYPE_HEADER)) {
            contentMimeType = rHeaders.getFirst(CONTENT_MIMETYPE_HEADER);
        }
        if(contentMimeType == null && rHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
            contentMimeType = rHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
        }
        if(contentMimeType != null && !contentMimeType.equals("")) {
            if(validMimetype(contentMimeType)) {
                userMetadata.put(StorageProvider.METADATA_CONTENT_MIMETYPE,
                                 contentMimeType);
            } else {
                throw new ResourceException("Invalid Mimetype");
            }
        }

        contentResource.updateContentMetadata(spaceID,
                                              contentID,
                                              contentMimeType,
                                              userMetadata,
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
            updateContentMetadata(spaceID, contentID, storeID);
            URI location = uriInfo.getRequestUri();
            Map<String, String> metadata = new HashMap<String, String>();
            metadata.put(StorageProvider.METADATA_CONTENT_CHECKSUM, checksum);
            return addContentMetadataToResponse(Response.created(location),
                                                metadata);

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