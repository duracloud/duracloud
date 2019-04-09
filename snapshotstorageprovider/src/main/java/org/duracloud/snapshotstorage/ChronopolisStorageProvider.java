/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshotstorage;

import java.util.Map;

import com.amazonaws.services.s3.AmazonS3;
import org.duracloud.storage.domain.StorageProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A snapshot storage provider which will move snapshot content
 * to the Chronopolis system.
 *
 * @author Bill Branan
 * Date: 06/07/2016
 */
public class ChronopolisStorageProvider extends SnapshotStorageProvider {

    private final Logger log =
        LoggerFactory.getLogger(ChronopolisStorageProvider.class);

    public ChronopolisStorageProvider(String accessKey, String secretKey,
                                      Map<String, String> options) {
        super(accessKey, secretKey, options);
    }

    public ChronopolisStorageProvider(AmazonS3 s3Client, String accessKey,
                                      Map<String, String> options) {
        super(s3Client, accessKey, options);
    }

    @Override
    public StorageProviderType getStorageProviderType() {
        return StorageProviderType.CHRONOPOLIS;
    }

}
