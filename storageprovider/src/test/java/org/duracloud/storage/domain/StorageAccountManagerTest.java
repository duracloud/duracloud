/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Bill Branan
 *         Date: 3/17/14
 */
public class StorageAccountManagerTest {

    @Test
    public void testGetEnv() {
        String host = "check.duracloud.org";
        String port = "9999";

        StorageAccountManager accountManager = new StorageAccountManager();
        accountManager.setEnvironment(host, port, "account");

        assertEquals(host, accountManager.getInstanceHost());
        assertEquals(port, accountManager.getInstancePort());
        assertEquals("account", accountManager.getAccountName());
    }

}
