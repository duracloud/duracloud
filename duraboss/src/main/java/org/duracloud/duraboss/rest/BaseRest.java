/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;

/**
 * @author: Bill Branan
 * Date: 5/11/11
 */
public class BaseRest {

    @Context
    HttpServletRequest request;

    @Context
    HttpHeaders headers;

    @Context
    UriInfo uriInfo;

    public static final String XML = MediaType.APPLICATION_XML;
    public static final MediaType APPLICATION_XML =
        MediaType.APPLICATION_XML_TYPE;
    public static final MediaType TEXT_PLAIN = MediaType.TEXT_PLAIN_TYPE;

    public static final String APP_NAME = "DuraBoss";

    protected Response responseOk(String text) {
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

    protected Response responseBad(String text, Response.Status status) {
        return Response.status(status).entity(text).build();
    }

}
