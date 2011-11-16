/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.storage.provider.StorageProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Iterator;
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
    public static final String SPACE_ACCESS_HEADER =
        HEADER_PREFIX + StorageProvider.PROPERTIES_SPACE_ACCESS;
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

    protected Map<String, String> getSpaceACLs() {
        return doGetUserProperties(SPACE_ACL_HEADER);
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

}
