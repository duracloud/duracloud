/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshotstorage;

import java.util.HashMap;

import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.domain.StorageProviderType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author bill
 *         Date: 6/7/2016
 */
public class DpnStorageProviderTest {

    @Test
    public void testGetStorageProviderType() {
        DpnStorageProvider provider = new DpnStorageProvider("accessKey", "secretKey");
        assertEquals(StorageProviderType.DPN, provider.getStorageProviderType());
    }

}
