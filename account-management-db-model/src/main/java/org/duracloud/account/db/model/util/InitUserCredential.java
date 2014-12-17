/*
 * Copyright (c) 2009-2011 DuraSpace. All rights reserved.
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
