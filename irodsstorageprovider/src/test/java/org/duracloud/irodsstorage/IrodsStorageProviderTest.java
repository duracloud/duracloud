/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.irodsstorage;

import java.util.HashMap;
import java.util.Map;

import org.duracloud.storage.domain.StorageProviderType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import static org.duracloud.storage.domain.StorageAccount.OPTS.BASE_DIRECTORY;
import static org.duracloud.storage.domain.StorageAccount.OPTS.HOST;
import static org.duracloud.storage.domain.StorageAccount.OPTS.PORT;
import static org.duracloud.storage.domain.StorageAccount.OPTS.RESOURCE;
import static org.duracloud.storage.domain.StorageAccount.OPTS.ZONE;

/**
 * @author Bill Branan
 * Date: 6/7/2016
 */
public class IrodsStorageProviderTest {

    @Test
    public void testGetStorageProviderType() {
        Map<String, String> options = new HashMap<>();
        options.put(BASE_DIRECTORY.name(), "base-directory");
        options.put(HOST.name(), "host");
        options.put(PORT.name(), "1234");
        options.put(RESOURCE.name(), "resource");
        options.put(ZONE.name(), "zone");

        IrodsStorageProvider provider =
            new IrodsStorageProvider("accessKey", "secretKey", options);
        assertEquals(StorageProviderType.IRODS, provider.getStorageProviderType());
    }

}
