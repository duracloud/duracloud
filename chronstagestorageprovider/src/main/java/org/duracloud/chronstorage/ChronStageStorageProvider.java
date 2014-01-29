/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chronstorage;

import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.s3storage.S3StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * A storage provider to act as a staging area for content that will
 * be transferred to Chronopolis.
 *
 * @author Bill Branan
 *         Date: 1/28/14
 */
public class ChronStageStorageProvider extends S3StorageProvider {

    private final Logger log =
        LoggerFactory.getLogger(ChronStageStorageProvider.class);

    public ChronStageStorageProvider(String accessKey, String secretKey) {
        super(accessKey, secretKey);
    }

    public ChronStageStorageProvider(String accessKey,
                                     String secretKey,
                                     Map<String, String> options) {
        super(accessKey, secretKey, options);
    }

    public ChronStageStorageProvider(AmazonS3Client s3Client,
                                     String accessKey,
                                     Map<String, String> options) {
         super(s3Client, accessKey, options);
    }

}
