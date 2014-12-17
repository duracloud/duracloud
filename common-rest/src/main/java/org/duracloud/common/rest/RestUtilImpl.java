/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.rest;

import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.List;


/**
 * Utility class for REST operations.
 *
 * @author Bill Branan
 */
public class RestUtilImpl implements RestUtil {

    protected final Logger log = LoggerFactory.getLogger(RestUtilImpl.class);

    /**
     * Retrieves the contents of the HTTP Request.
     * @return InputStream from the request
     */
    @Override
    public RequestContent getRequestContent(HttpServletRequest request,
                                            HttpHeaders headers)
    throws Exception {
        RequestContent rContent = null;

        // See if the request is a multi-part file upload request
        if(ServletFileUpload.isMultipartContent(request)) {

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload();

            // Parse the request, use the first available File item
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                if (!item.isFormField()) {
                    rContent = new RequestContent();
                    rContent.contentStream = item.openStream();
                    rContent.mimeType = item.getContentType();

                    FileItemHeaders itemHeaders = item.getHeaders();
                    if(itemHeaders != null) {
                        String contentLength =
                            itemHeaders.getHeader("Content-Length");
                        if(contentLength != null) {
                            rContent.size = Long.parseLong(contentLength);
                        }
                    }

                    break;
                }
            }
        } else {
            // If the content stream was not found as a multipart,
            // try to use the stream from the request directly
            rContent = new RequestContent();
            rContent.contentStream = request.getInputStream();
            if (request.getContentLength() >= 0) {
              rContent.size = request.getContentLength();
            }
        }

        // Attempt to set the mime type and size if not already set
        if(rContent != null) {
            if(rContent.mimeType == null) {
                MediaType mediaType = headers.getMediaType();
                if(mediaType != null) {
                    rContent.mimeType = mediaType.toString();
                }
            }

            if(rContent.size == 0) {
                List<String> lengthHeaders =
                    headers.getRequestHeader("Content-Length");
                if(lengthHeaders != null && lengthHeaders.size() > 0) {
                    rContent.size = Long.parseLong(lengthHeaders.get(0));
                }
            }
        }

        return rContent;
    }

}
