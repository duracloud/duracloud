/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshotstorage;

import org.duracloud.storage.domain.StorageProviderType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author bill
 *         Date: 6/7/2016
 */
public class ChronopolisStorageProviderTest {

    @Test
    public void testGetStorageProviderType() {
        ChronopolisStorageProvider provider =
            new ChronopolisStorageProvider("accessKey", "secretKey");
        assertEquals(StorageProviderType.CHRONOPOLIS, provider.getStorageProviderType());
    }

}
