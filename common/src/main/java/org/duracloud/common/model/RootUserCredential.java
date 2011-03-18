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
    private static final String encodedPassword = "6dde532e82159e351f3b9b685073aa8531b0f11d64a13d6c7947e66e0fb0ef5c";

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

    static public String getRootEncodedPassword() {
        return encodedPassword;
    }

}
