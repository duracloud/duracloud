/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.model;

import org.duracloud.common.util.ChecksumUtil;

/**
 * @author Andrew Woods
 *         Date: Apr 18, 2010
 */
public class RootUserCredential extends Credential {

    private static final String defaultUsername = "root";
    private static final String defaultPassword = "rpw";
    private static final String defaultEmail = "no-root-password-set";
    public RootUserCredential() {
        super(getRootUsername(), getRootPassword());
    }

    public static String getRootUsername() {
        return System.getProperty("root.username", defaultUsername);
    }
    
    public static String getRootEmail() {
        return System.getProperty("root.email", defaultEmail);
    }

    private static String getRootPassword() {
        return System.getProperty("root.password", defaultPassword);
    }

    public String getRootEncodedPassword() {
        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.SHA_256);
        return util.generateChecksum(getRootPassword());
    }

}
