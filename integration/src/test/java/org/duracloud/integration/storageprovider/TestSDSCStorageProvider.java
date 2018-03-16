/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.storageprovider;

import java.io.IOException;

import junit.framework.Assert;
import org.duracloud.common.model.SimpleCredential;
import org.duracloud.common.test.StorageProviderCredential;
import org.duracloud.common.test.TestConfigUtil;
import org.duracloud.sdscstorage.SDSCStorageProvider;
import org.duracloud.storage.provider.StorageProvider;

/**
 * @author Erik Paulsson
 * Date: 8/5/13
 */
public class TestSDSCStorageProvider extends TestStorageProvider {

    @Override
    public StorageProvider createStorageProvider() {
        TestConfigUtil testConfigUtil = new TestConfigUtil();
        try {
            SimpleCredential cred = testConfigUtil.getCredential(
                StorageProviderCredential.ProviderType.SDSC);
            storageProvider = new SDSCStorageProvider(cred.getUsername(),
                                                      cred.getPassword());
            return storageProvider;
        } catch (IOException ioe) {
            Assert.fail(ioe.getMessage());
            return null;
        }
    }

}
