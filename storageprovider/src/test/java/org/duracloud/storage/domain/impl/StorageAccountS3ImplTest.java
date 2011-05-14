/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain.impl;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: 5/13/11
 */
public class StorageAccountS3ImplTest {

    @Test
    public void testGetStorageClass() throws Exception {
        StorageAccountS3Impl acct = createAccount(null);

        String storageClass = acct.getStorageClass();
        Assert.assertNull(storageClass);

        acct.setStorageClassStandard();
        storageClass = acct.getStorageClass();
        Assert.assertEquals("STANDARD", storageClass);

        acct.setStorageClassReducedRedundancy();
        storageClass = acct.getStorageClass();
        Assert.assertEquals("REDUCED_REDUNDANCY", storageClass);
    }

    @Test
    public void testConstructor() {
        StorageAccountS3Impl acct = createAccount(null);
        acct = createAccount("ReducEDreDUNdanCY");
        acct = createAccount("stanDard");
        acct = createAccount("");

        try {
            acct = createAccount("junk");
            Assert.fail("exception expected");
        } catch (Exception e) {
            // do nothing
        }
    }

    private StorageAccountS3Impl createAccount(String storageClass) {
        String id = "id";
        String username = "username";
        String password = "password";
        return new StorageAccountS3Impl(id, username, password, storageClass);
    }
}
