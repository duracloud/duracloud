/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.apache.http.HttpStatus;
import org.duracloud.audit.logger.ClientInfoLogger;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.rest.HttpHeaders;
import org.duracloud.common.rest.RestUtil;
import org.duracloud.common.web.EncodeUtil;
import org.duracloud.durastore.error.ResourceChecksumException;
import org.duracloud.durastore.error.ResourceException;
import org.duracloud.durastore.error.ResourceNotFoundException;
import org.duracloud.durastore.error.ResourceStateException;
import org.duracloud.storage.error.InvalidIdException;
import org.duracloud.storage.error.InvalidRequestException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
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
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Provides interaction with content via REST
 *
 * @author Bill Branan
 */
@Path("/{spaceID: (?!acl/)[^/]+}/{contentID: [^?]+}")
@Component
public class ContentRest extends BaseRest {
    private final Logger log = LoggerFactory.getLogger(ContentRest.class);

    private ContentResource contentResource;
    private RestUtil restUtil;
    private final ClientInfoLogger clientInfoLog = new ClientInfoLogger();
    @Autowired
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
            return responseNotFound(msg.toString(), e, NOT_FOUND);

        } catch (ResourceStateException e) {
            return responseBad(msg.toString(), e, CONFLICT);

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
            return responseNotFound(msg.toString(), e, NOT_FOUND);

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
    protected Response addContentPropertiesToResponse(ResponseBuilder response,
                                                      Map<String, String> properties) {
        if(properties != null) {
            // Set Content-Type header
            String contentMimetype = // content-mimetype header
                properties.remove(StorageProvider.PROPERTIES_CONTENT_MIMETYPE);
            String contentType = // Content-Type header
                properties.remove(HttpHeaders.CONTENT_TYPE);
            if(null != contentMimetype && validMimetype(contentMimetype)) {
                response.header(HttpHeaders.CONTENT_TYPE, contentMimetype);
            } else if(null != contentType && validMimetype(contentType)) {
                response.header(HttpHeaders.CONTENT_TYPE, contentType);
            } else {
                response.header(HttpHeaders.CONTENT_TYPE, DEFAULT_MIME);
            }

            // Set Content-Length header
            String contentSize = // content-size header
                properties.remove(StorageProvider.PROPERTIES_CONTENT_SIZE);
            String contentLength = // Content-Length header
                properties.remove(HttpHeaders.CONTENT_LENGTH);
            if(null != contentSize) {
                response.header(HttpHeaders.CONTENT_LENGTH, contentSize);
            } else if(null != contentLength) {
                response.header(HttpHeaders.CONTENT_LENGTH, contentLength);
            }

            // Set Last-Modified header
            String contentModified = // content-modified header
                properties.remove(StorageProvider.PROPERTIES_CONTENT_MODIFIED);
            String lastModified = // Last-Modified header
                properties.remove(HttpHeaders.LAST_MODIFIED);
            if(null != contentModified) {
                response.header(HttpHeaders.LAST_MODIFIED, contentModified);
            } else if(null != lastModified) {
                response.header(HttpHeaders.LAST_MODIFIED, lastModified);
            }

            // Set ETag and Content-MD5 headers
            String contentChecksum = // content-checksum header
                properties.remove(StorageProvider.PROPERTIES_CONTENT_CHECKSUM);
            String contentMdFive = // content-md5 header
                properties.remove(StorageProvider.PROPERTIES_CONTENT_MD5);
            String contentMd5 = // Content-MD5 header
                properties.remove(HttpHeaders.CONTENT_MD5);
            String etag = // ETag header
                properties.remove(HttpHeaders.ETAG);
            if(null != contentChecksum) {
                response.header(HttpHeaders.CONTENT_MD5, contentChecksum);
                response.header(HttpHeaders.ETAG, contentChecksum);
            } else if(null != contentMdFive) {
                response.header(HttpHeaders.CONTENT_MD5, contentMdFive);
                response.header(HttpHeaders.ETAG, contentMdFive);
            } else if(null != contentMd5) {
                response.header(HttpHeaders.CONTENT_MD5, contentMd5);
                response.header(HttpHeaders.ETAG, contentMd5);
            } else if(null != etag) {
                response.header(HttpHeaders.CONTENT_MD5, etag);
                response.header(HttpHeaders.ETAG, etag);
            }

            // Set the remaining property values as headers
            Iterator<String> propertiesNames = properties.keySet().iterator();
            while(propertiesNames.hasNext()) {
                String propertiesName = propertiesNames.next();
                String propertiesValue = properties.get(propertiesName);

                if(propertiesName.equals(HttpHeaders.DATE) ||
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
            return responseNotFound(msg.toString(), e, NOT_FOUND);

        } catch (ResourceStateException e) {
            return responseBad(msg.toString(), e, CONFLICT);

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

        // Set mimetype in properties if it was provided
        String contentMimeType = null;
        if(rHeaders.containsKey(CONTENT_MIMETYPE_HEADER)) {
            contentMimeType = rHeaders.getFirst(CONTENT_MIMETYPE_HEADER);
        }
        if(contentMimeType == null && rHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
            contentMimeType = rHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
        }

        contentResource.updateContentProperties(spaceID,
                                                contentID,
                                                contentMimeType,
                                                getProperties(contentMimeType),
                                                storeID);
        String responseText = "Content " + contentID + " updated successfully";
        return Response.ok(responseText, TEXT_PLAIN).build();
    }

    private Map<String, String> getProperties(String contentMimeType)
        throws ResourceException {
        Map<String, String> userProperties =
            getUserProperties(CONTENT_MIMETYPE_HEADER);

        if(contentMimeType != null && !contentMimeType.equals("")) {
            if(validMimetype(contentMimeType)) {
                userProperties.put(StorageProvider.PROPERTIES_CONTENT_MIMETYPE,
                                 contentMimeType);
            } else {
                throw new ResourceException("Invalid Mimetype");
            }
        }

        return userProperties;
    }

    /**
     * see ContentResource.addContent() and ContentResource.copyContent().
     * 
     * @return 201 response indicating content added/copied successfully
     */
    @PUT
    public Response putContent(@PathParam("spaceID") String spaceID,
                               @PathParam("contentID") String contentID,
                               @QueryParam("storeID") String storeID,
                               @HeaderParam(BaseRest.COPY_SOURCE_HEADER) String copySource,
                               @HeaderParam(BaseRest.COPY_SOURCE_STORE_HEADER) String sourceStoreID) {
        if (null != copySource) {
            return copyContent(spaceID, contentID, storeID, sourceStoreID, copySource);

        } else {
            return addContent(spaceID, contentID, storeID);
        }
    }

    /**
     * see ContentResource.addContent()
     * @return 201 response indicating content added successfully
     */
    private Response addContent(String spaceID,
                               String contentID,
                               String storeID) {
        StringBuilder msg = new StringBuilder("adding content(");
        msg.append(spaceID);
        msg.append(", ");
        msg.append(contentID);
        msg.append(", ");
        msg.append(storeID);
        msg.append(")");
        try {
            log.info(msg.toString());
            
            return doAddContent(spaceID, contentID, storeID);

        } catch (InvalidIdException e) {
            return responseBad(msg.toString(), e, BAD_REQUEST);

        } catch (ResourceNotFoundException e) {
            return responseNotFound(msg.toString(), e, NOT_FOUND);

        } catch (ResourceChecksumException e) {
            return responseBad(msg.toString(), e, CONFLICT);

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
        
        logClientInfo(rHeaders);

        if(rHeaders.containsKey(HttpHeaders.CONTENT_MD5)) {
            checksum = rHeaders.getFirst(HttpHeaders.CONTENT_MD5);
        }

        if (content != null) {
            checksum = contentResource.addContent(spaceID,
                                                  contentID,
                                                  content.getContentStream(),
                                                  content.getMimeType(),
                                                  getProperties(content.getMimeType()),
                                                  content.getSize(),
                                                  checksum,
                                                  storeID);
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

    private void logClientInfo(MultivaluedMap<String, String> rHeaders) {
        Map<String,String> clientInfo = new HashMap<>();
        clientInfo.put("accountId", getAccountId());
        clientInfo.put("clientVersion",
                       rHeaders.containsKey(Constants.CLIENT_VERSION_HEADER)
                           ? rHeaders.getFirst(Constants.CLIENT_VERSION_HEADER)
                           : "none-specified");
        clientInfoLog.log(clientInfo);
    }

    /**
     * see ContentResource.copyContent()
     *
     * @return 201 response indicating content copied successfully
     */
    private Response copyContent(String spaceID,
                                 String contentID,
                                 String storeID,
                                 String sourceStoreID,
                                 String copySource) {
        StringBuilder msg = new StringBuilder("copying content from (");
        msg.append(copySource);
        msg.append(") to (");
        msg.append(spaceID);
        msg.append(" / ");
        msg.append(contentID);
        msg.append(", ");
        msg.append(storeID);
        msg.append(")");
        log.info(msg.toString());

        try {
            return doCopyContent(spaceID, contentID, storeID, sourceStoreID, copySource);

        } catch (InvalidIdException e) {
            return responseBad(msg.toString(), e, BAD_REQUEST);

        } catch (InvalidRequestException e) {
            return responseBad(msg.toString(), e, BAD_REQUEST);

        } catch (ResourceNotFoundException e) {
            return responseNotFound(msg.toString(), e, NOT_FOUND);

        } catch (ResourceStateException e) {
            return responseBad(msg.toString(), e, CONFLICT);

        } catch (ResourceException e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            return responseBad(msg.toString(), e, INTERNAL_SERVER_ERROR);
        }
    }

    private Response doCopyContent(String destSpaceID,
                                   String destContentID,
                                   String storeID,
                                   String sourceStoreID,
                                   String copySource) throws Exception {
        StringBuilder msg = new StringBuilder();

        // Verify no body to request.
        RestUtil.RequestContent content = restUtil.getRequestContent(request,
                                                                     headers);
        if (null != content && content.getSize() > 0) {
            msg.append("Body should not be present in copy-content request:");
            msg.append(" from ");
            msg.append(copySource);
            msg.append(", with body size: ");
            msg.append(content.getSize());
            log.error(msg.toString());
            throw new InvalidRequestException(msg.toString());
        }

        if (null == copySource || copySource.isEmpty()) {
            msg.append("Missing header: ");
            msg.append(COPY_SOURCE_HEADER);
            log.error(msg.toString());
            throw new InvalidRequestException(msg.toString());
        }
        
        String srcSpaceID = getSpaceId(copySource);
        String srcContentID = EncodeUtil.urlDecode(getContentId(copySource));
        if (null == srcSpaceID || null == srcContentID) {
            msg.append("Malformed ");
            msg.append(COPY_SOURCE_HEADER);
            msg.append(" header: ");
            msg.append(copySource);
            log.error(msg.toString());
            throw new InvalidRequestException(msg.toString());
        }

        // Do the underlying copy.
        msg.append("copying content from (store[");
        msg.append(sourceStoreID);
        msg.append("]: ");
        msg.append(srcSpaceID);
        msg.append(" / ");
        msg.append(srcContentID);
        msg.append(")");
        log.info(msg.toString());
        String checksum = contentResource.copyContent(sourceStoreID,
                                                      srcSpaceID,
                                                      srcContentID,
                                                      storeID,
                                                      destSpaceID,
                                                      destContentID);

        // Construct the response.
        URI location = uriInfo.getRequestUri();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(StorageProvider.PROPERTIES_CONTENT_CHECKSUM, checksum);
        return addContentPropertiesToResponse(Response.created(location),
                                              properties);
    }

    private String getSpaceId(String copySource) {
        String[] spaceAndContent = splitSpaceAndContentIds(copySource);
        return null == spaceAndContent ? null : spaceAndContent[0];
    }

    private String getContentId(String copySource) {
        String[] spaceAndContent = splitSpaceAndContentIds(copySource);
        return null == spaceAndContent ? null : spaceAndContent[1];
    }

    private String[] splitSpaceAndContentIds(String copySource) {
        if (null == copySource || copySource.isEmpty()) {
            return null;
        }

        while (copySource.charAt(0) == '/') {
            copySource = copySource.substring(1, copySource.length());
        }

        int contentStartIndex = copySource.indexOf("/");
        if (-1 == contentStartIndex || contentStartIndex == copySource.length() - 1) {
            return null;
        }

        String[] spaceAndContent = new String[2];
        spaceAndContent[0] = copySource.substring(0, contentStartIndex);
        spaceAndContent[1] = copySource.substring(contentStartIndex + 1,
                                                  copySource.length());
        return spaceAndContent;
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
            return responseNotFound(msg.toString(), e, NOT_FOUND);

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