/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

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
}
