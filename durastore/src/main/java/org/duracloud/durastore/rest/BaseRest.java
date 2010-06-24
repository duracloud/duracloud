/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.duracloud.storage.provider.StorageProvider;

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

    public static final String XML = "text/xml";
    public static final String HTML = "text/html";

    public static final MediaType TEXT_XML = new MediaType("text", "xml");
    public static final MediaType TEXT_HTML = new MediaType("text", "html");
    public static final MediaType TEXT_PLAIN = new MediaType("text", "plain");

    public static final String DEFAULT_MIME = "application/octet-stream";

    public static final String HEADER_PREFIX = "x-dura-meta-";
    public static final String SPACE_ACCESS_HEADER =
        HEADER_PREFIX + StorageProvider.METADATA_SPACE_ACCESS;
    public static final String CONTENT_MIMETYPE_HEADER =
        HEADER_PREFIX + StorageProvider.METADATA_CONTENT_MIMETYPE;

    /**
     * Looks through the request headers and pulls out user metadata.
     * Only includes items which are not in the exceptions list.
     */
    protected Map<String, String> getUserMetadata(String... exceptions) {
        MultivaluedMap<String, String> rHeaders = headers.getRequestHeaders();
        Map<String, String> userMetadata = new HashMap<String, String>();
        Iterator<String> headerNames = rHeaders.keySet().iterator();
        while(headerNames.hasNext()) {
            String headerName = headerNames.next();
            if(headerName.startsWith(HEADER_PREFIX)) {
                boolean include = true;
                for(String exception : exceptions) {
                    if(exception.equals(headerName)) {
                        include = false;
                    }
                }
                if(include) {
                    String noPrefixName =
                        headerName.substring(HEADER_PREFIX.length());
                    userMetadata.put(noPrefixName,
                                     rHeaders.getFirst(headerName));
                }
            }
        }
        return userMetadata;
    }
}
