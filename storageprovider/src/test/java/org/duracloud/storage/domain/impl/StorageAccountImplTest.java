/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain.impl;

import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: 5/13/11
 */
public class StorageAccountImplTest {

    private StorageAccountImpl acct;

    @Before
    public void setUp() throws Exception {
        String id = "id";
        String username = "username";
        String password = "password";
        StorageProviderType type = StorageProviderType.RACKSPACE;
        acct = new StorageAccountImpl(id, username, password, type);
    }

    @Test
    public void testSetProperty() throws Exception {
        boolean success = false;
        String val = "value";
        doTestSetProperty(null, val, success);
        doTestSetProperty("junk", val, success);

        success = true;
        doTestSetProperty(StorageAccount.PROPS.STORAGE_CLASS.name(),
                          val,
                          success);
    }

    private void doTestSetProperty(String key, String value, boolean success) {
        boolean valid = true;
        try {
            acct.setProperty(key, value);
            Assert.assertTrue(success);

        } catch (Exception e) {
            valid = false;
        }
        Assert.assertEquals(success, valid);
    }

    @Test
    public void testGetProperty() throws Exception {
        boolean success = false;
        String prop = doTestGetProperty(null, success);
        Assert.assertNull(prop);

        prop = doTestGetProperty("junk", success);
        Assert.assertNull(prop);

        success = true;
        prop = doTestGetProperty(StorageAccount.PROPS.STORAGE_CLASS.name(),
                                 true);
        Assert.assertNull(prop);
    }

    private String doTestGetProperty(String key, boolean success) {
        boolean valid = true;
        String prop = null;
        try {
            prop = acct.getProperty(key);
            Assert.assertTrue(success);

        } catch (Exception e) {
            valid = false;
        }
        Assert.assertEquals(success, valid);
        return prop;
    }
}
