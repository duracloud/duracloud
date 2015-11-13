/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.model.AclType;
import org.duracloud.storage.provider.StorageProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Base REST resource
 *
 * @author Bill Branan
 */
public abstract class BaseRest {
    @Context
    HttpServletRequest request;

    @Context
    HttpHeaders headers;

    @Context
    UriInfo uriInfo;

    public static final String XML = MediaType.APPLICATION_XML;
    public static final String HTML = MediaType.TEXT_HTML;

    public static final MediaType APPLICATION_XML =
        MediaType.APPLICATION_XML_TYPE;
    public static final MediaType TEXT_PLAIN = MediaType.TEXT_PLAIN_TYPE;

    public static final String DEFAULT_MIME = MediaType.APPLICATION_OCTET_STREAM;

    public static final String HEADER_PREFIX = "x-dura-meta-";
    public static final String SPACE_ACL_HEADER =
        HEADER_PREFIX + StorageProvider.PROPERTIES_SPACE_ACL;
    public static final String CONTENT_MIMETYPE_HEADER =
        HEADER_PREFIX + StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
    public static final String COPY_SOURCE_HEADER =
        HEADER_PREFIX + StorageProvider.PROPERTIES_COPY_SOURCE;
    public static final String COPY_SOURCE_STORE_HEADER = 
        HEADER_PREFIX + StorageProvider.PROPERTIES_COPY_SOURCE_STORE;

    public static final String APP_NAME = "DuraStore";


    /**
     * Looks through the request headers and pulls out user properties.
     * Only includes items which are not in the exclusions list.
     */
    protected Map<String, String> getUserProperties(String... exclusions) {
        return doGetUserProperties(HEADER_PREFIX, exclusions);
    }

    protected Map<String, String> getUserProperties() {
        return doGetUserProperties(HEADER_PREFIX);
    }

    protected Map<String, AclType> getSpaceACLs() {
        Map<String, AclType> acls = new HashMap<String, AclType>();
        Map<String, String> aclProps = doGetUserProperties(SPACE_ACL_HEADER);
        for (String key : aclProps.keySet()) {
            String val = aclProps.get(key);
            acls.put(key, AclType.valueOf(val));
        }
        return acls;
    }

    private Map<String, String> doGetUserProperties(String prefix,
                                                    String... exclusions) {
        MultivaluedMap<String, String> rHeaders = headers.getRequestHeaders();
        Map<String, String> userProperties = new HashMap<String, String>();

        for (String headerName : rHeaders.keySet()) {
            if (headerName.startsWith(prefix)) {
                boolean include = true;
                for (String exclusion : exclusions) {
                    if (exclusion.equals(headerName)) {
                        include = false;
                    }
                }
                if (include) {
                    String noPrefixName =
                        headerName.substring(HEADER_PREFIX.length());
                    userProperties.put(noPrefixName, rHeaders.getFirst(
                        headerName));
                }
            }
        }
        return userProperties;
    }
    
    protected Response responseOk() {
        return Response.ok().build();
    }

    protected Response responseOk(String text) {
        return Response.ok(text, TEXT_PLAIN).build();
    }

    protected Response responseOkStream(InputStream text) {
        return Response.ok(text, TEXT_PLAIN).build();
    }

    protected Response responseOkXml(String xml) {
        return Response.ok(xml, APPLICATION_XML).build();
    }

    protected Response responseOkXmlStream(InputStream xml) {
        return Response.ok(xml, APPLICATION_XML).build();
    }

    protected Response responseNotFound() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    protected Response responseNotFound(String msg) {
        return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
    }

    protected Response responseBadRequest(Exception e) {
        return responseBad(e, Response.Status.BAD_REQUEST);
    }

    protected Response responseBad(Exception e) {
        return responseBad(e, Response.Status.INTERNAL_SERVER_ERROR);
    }

    protected Response responseBad(Exception e, Response.Status status) {
        String text = e.getMessage() == null ? "null" : e.getMessage();
        return responseBad(text, status);
    }

    protected Response responseBad(String msg, Response.Status status) {
        String entity = msg == null ? "null" : msg;
        return Response.status(status).entity(entity).build();
    }

    protected String getSubdomain() {
        String subdomain = request.getHeader("X-FORWARDED-HOST");
        if(subdomain == null){
            subdomain = request.getServerName();
        }
        
        subdomain = subdomain.split("[.]")[0];
        return subdomain;
    }




}
