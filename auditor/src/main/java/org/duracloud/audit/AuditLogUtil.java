/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit;

import org.apache.commons.lang3.StringUtils;

/**
 * A grab bag of useful common functions.
 * 
 * @author Daniel Bernstein
 * 
 */
public class AuditLogUtil {
    public static final String[] AUDIT_LOG_COLUMNS = { "ACCOUNT",
                                                      "STORE_ID",
                                                      "SPACE_ID",
                                                      "CONTENT_ID",
                                                      "CONTENT_MD5",
                                                      "CONTENT_SIZE",
                                                      "CONTENT_MIMETYPE",
                                                      "CONTENT_PROPERTIES",
                                                      "SPACE_ACLS",
                                                      "SOURCE_SPACE_ID",
                                                      "SOURCE_CONTENT_ID",
                                                      "TIMESTAMP",
                                                      "ACTION",
                                                      "USERNAME" };

    public static String getHeader() {
        String header =  StringUtils.join(AUDIT_LOG_COLUMNS, "\t");
        
        return header;
    }
}
