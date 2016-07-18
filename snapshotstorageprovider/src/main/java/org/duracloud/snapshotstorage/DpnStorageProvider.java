/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshotstorage;

import java.util.Map;

import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storage.StoragePolicy;
import org.duracloud.storage.domain.StorageProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A snapshot storage provider which will move snapshot content
 * to the DPN system.
 *
 * @author Bill Branan
 *         Date: 06/07/2016
 */
public class DpnStorageProvider extends SnapshotStorageProvider {

    private final Logger log =
        LoggerFactory.getLogger(DpnStorageProvider.class);

    public DpnStorageProvider(String accessKey, String secretKey) {
        super(accessKey, secretKey);
    }

    public DpnStorageProvider(String accessKey, String secretKey,
                              Map<String, String> options) {
        super(accessKey, secretKey, options);
    }

    public DpnStorageProvider(AmazonS3Client s3Client, String accessKey,
                              Map<String, String> options) {
         super(s3Client, accessKey, options);
    }

    @Override
    public StorageProviderType getStorageProviderType() {
        return StorageProviderType.DPN;
    }

}
