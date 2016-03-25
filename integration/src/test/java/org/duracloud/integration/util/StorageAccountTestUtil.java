/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.util;

import org.duracloud.common.model.Credential;

/**
 * Provides utilities for testing with storage accounts.
 *
 * @author Bill Branan
 */
public class StorageAccountTestUtil {

    private Credential rootCredential;

    public Credential getRootCredential() {
        return new Credential(System.getProperty("root.username", "root"), System.getProperty("root.password", "rpw"));
    }

}
