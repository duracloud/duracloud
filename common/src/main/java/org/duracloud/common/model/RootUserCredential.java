/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.model;

/**
 * @author Andrew Woods
 *         Date: Apr 18, 2010
 */
public class RootUserCredential extends Credential {

    private static final String defaultUsername = "root";
    private static final String defaultPassword = "rpw";

    public RootUserCredential() {
        super(getRootUsername(), getRootPassword());
    }

    static String getRootUsername() {
        String username = System.getProperty("root.username");
        if (null == username) {
            username = defaultUsername;
        }
        return username;
    }

    static String getRootPassword() {
        String password = System.getProperty("root.password");
        if (null == password) {
            password = defaultPassword;
        }
        return password;
    }

}
