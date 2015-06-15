/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model.util;

import org.duracloud.common.model.Credential;
import org.duracloud.common.util.ChecksumUtil;

/**
 * @author Andrew Woods
 *         Date: Jan 31, 2011
 */
public class InitUserCredential extends Credential {

    private static final String defaultUsername = "init";
    private static final String defaultPassword = "ipw";

    public InitUserCredential() {
        super(getInitUsername(), getInitPassword());
    }

    private static String getInitUsername() {
        String username = System.getProperty("init.username");
        if (null == username) {
            username = defaultUsername;
        }
        return username;
    }

    private static String getInitPassword() {
        String password = System.getProperty("init.password");
        if (null == password) {
            password = defaultPassword;
        }
        return password;
    }

    public String getInitEncodedPassword() {
        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.SHA_256);
        return util.generateChecksum(getInitPassword());
    }
}
