/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.error;

/**
 * @author Andrew Woods
 * Date: 4/20/11
 */
public class InvalidUsernameException extends DuraCloudRuntimeException {
    public InvalidUsernameException(String username) {
        super("Invalid username: " + username);
    }
}
