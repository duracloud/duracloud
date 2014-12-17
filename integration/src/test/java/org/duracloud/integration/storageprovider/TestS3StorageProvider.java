/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.storageprovider;

import junit.framework.Assert;
import org.duracloud.common.model.SimpleCredential;
import org.duracloud.common.test.StorageProviderCredential;
import org.duracloud.common.test.TestConfigUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.provider.StorageProvider;

import java.io.IOException;

/**
 * @author Bill Branan
 *         Date: 9/8/14
 */
public class TestS3StorageProvider extends TestStorageProvider {

    @Override
    public StorageProvider createStorageProvider() {
        TestConfigUtil testConfigUtil = new TestConfigUtil();
        try {
            SimpleCredential cred = testConfigUtil.getCredential(
                StorageProviderCredential.ProviderType.AMAZON_S3);
            storageProvider = new S3StorageProvider(cred.getUsername(),
                                                    cred.getPassword());
            return storageProvider;
        } catch (IOException ioe) {
            Assert.fail(ioe.getMessage());
            return null;
        }
    }

}
