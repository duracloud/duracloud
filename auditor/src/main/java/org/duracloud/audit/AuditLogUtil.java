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
 */
public class AuditLogUtil {

    private AuditLogUtil() {
        // Ensures no instances are made of this class, as there are only static members.
    }

    public static final String[] AUDIT_LOG_COLUMNS = {"account",
                                                      "store-id",
                                                      "space-id",
                                                      "content-id",
                                                      "content-md5",
                                                      "content-size",
                                                      "content-mimetype",
                                                      "content-properties",
                                                      "space-acls",
                                                      "source-space-id",
                                                      "source-content-id",
                                                      "timestamp",
                                                      "action",
                                                      "username"};

    public static String getHeader() {
        String header = StringUtils.join(AUDIT_LOG_COLUMNS, "\t");

        return header;
    }
}
