/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.rest;

/**
 * @author: Bill Branan
 * Date: Apr 14, 2010
 */
public interface HttpHeaders extends javax.ws.rs.core.HttpHeaders {

    public static final String AGE = "Age";
    public static final String CONNECTION = "Connection";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_MD5 = "Content-MD5";
    public static final String CONTENT_RANGE = "Content-Range";
    public static final String PRAGMA = "Pragma";
    public static final String RETRY_AFTER = "Retry-After";
    public static final String SERVER = "Server";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String UPGRADE = "Upgrade";
    public static final String WARNING = "Warning";
    public static final String X_FORWARDED_HOST = "X-FORWARDED-HOST";
    
}
