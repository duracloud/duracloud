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

    private static final String defaultUsernameProp = "root.username";
    private static final String defaultPasswordProp = "root.password";
    private static final String defaultEmailProp = "root.email";

    private static String usernameProp = defaultUsernameProp;
    private static String passwordProp = defaultPasswordProp;
    private static String emailProp = defaultEmailProp;

    
    private static final String defaultUsername = "root";
    private static final String defaultPassword = "rpw";
    private static final String defaultEmail = "no-root-password-set";
    public RootUserCredential() {
        super(getRootUsername(), getRootPassword());
    }

    /**
     * Allows users to override the default  username, password, and email
     * System property keys. 
     * @param usernameProp
     * @param passwordProp
     * @param emailProp
     */
    public static void overrideSystemPropertyKeys(String usernameProp,
                                       String passwordProp,
                                       String emailProp) {
        RootUserCredential.usernameProp = userDefaultIfNull(usernameProp, defaultUsernameProp);
        RootUserCredential.passwordProp = userDefaultIfNull(passwordProp, defaultPasswordProp);
        RootUserCredential.emailProp = userDefaultIfNull(emailProp, defaultEmailProp);
    }
    
    private static String userDefaultIfNull(String prop,
                                            String defaultProp) {
        return prop != null ? prop : defaultProp;
    }

    public static String getRootUsername() {
        return getProperty(usernameProp, defaultUsername);
    }
    
    public static String getRootEmail() {
        return getProperty(emailProp, defaultEmail);
    }

    private static String getRootPassword() {
        return getProperty(passwordProp, defaultPassword);
    }

    private static String getProperty(String propertyKey,
                                      String defaultValue) {
       return System.getProperty(propertyKey, defaultValue);
    }

    public String getRootEncodedPassword() {
        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.SHA_256);
        return util.generateChecksum(getRootPassword());
    }

}
