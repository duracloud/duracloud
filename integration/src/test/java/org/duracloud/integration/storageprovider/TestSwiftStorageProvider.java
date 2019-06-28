/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.storageprovider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.duracloud.common.model.SimpleCredential;
import org.duracloud.common.test.StorageProviderCredential;
import org.duracloud.common.test.TestConfigUtil;
import org.duracloud.storage.domain.StorageAccount.OPTS;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.swiftstorage.SwiftStorageProvider;

/**
 * @author Andy Foster
 * Date: May 7, 2019
 */
public class TestSwiftStorageProvider extends TestStorageProvider {

    @Override
    public StorageProvider createStorageProvider() {
        TestConfigUtil testConfigUtil = new TestConfigUtil();
        try {
            SimpleCredential cred = testConfigUtil.getCredential(
                StorageProviderCredential.ProviderType.SWIFT_S3);
            Map<String, String> options = new HashMap<String, String>();
            options.put(OPTS.SWIFT_S3_ENDPOINT.name(), testConfigUtil.getSwiftEndpoint());
            options.put(OPTS.SWIFT_S3_SIGNER_TYPE.name(), testConfigUtil.getSwiftSignerType());
            storageProvider = new SwiftStorageProvider(cred.getUsername(),
                                                    cred.getPassword(), options);
            return storageProvider;
        } catch (IOException ioe) {
            // Providing these credentials is optional, so don't fail here like
            // we do in TestS3StorageProvider.
            return null;
        }
    }

    @Override
    public void testCopyContentSameSpaceSameName() {
        // This throws AmazonS3Exception because the requests are the
        // same, so omitting this test.
    }
}
