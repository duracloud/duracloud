/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.rest;

import org.apache.commons.io.input.AutoCloseInputStream;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 * @author Andrew Woods
 *         Date: Aug 31, 2010
 */
public interface RestUtil {
    RestUtil.RequestContent getRequestContent(HttpServletRequest request,
                                              javax.ws.rs.core.HttpHeaders headers)
        throws Exception;


    public class RequestContent {
        protected InputStream contentStream = null;
        protected String mimeType = null;
        protected long size = 0;

        /**
         * @return the contentStream
         */
        public InputStream getContentStream() {
            return new AutoCloseInputStream(contentStream);
        }

        /**
         * @return the mimeType
         */
        public String getMimeType() {
            return mimeType;
        }

        /**
         * @return the size
         */
        public long getSize() {
            return size;
        }
    }
}
