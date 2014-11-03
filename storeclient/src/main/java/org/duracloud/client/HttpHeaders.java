/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

/**
 * @author: Bill Branan
 * Date: Sep 24, 2010
 */
public interface HttpHeaders {

    public static final String CONTENT_MD5 = "Content-MD5";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ETAG = "ETag";
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String BIT_INTEGRITY_REPORT_RESULT = "Bit-Integrity-Report-Result";
    public static final String BIT_INTEGRITY_REPORT_COMPLETION_DATE = "Bit-Integrity-Report-Completion-Date";
    
}
