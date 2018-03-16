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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A storage provider to act as a staging area for content that will
 * be transferred to an external storage system as a "snapshot". A "snapshot"
 * in this case is a set of content that is captured as a whole at a point
 * in time.
 *
 * @author Bill Branan
 * Date: 1/28/14
 */
public abstract class SnapshotStorageProvider extends S3StorageProvider {

    private final Logger log =
        LoggerFactory.getLogger(SnapshotStorageProvider.class);

    public SnapshotStorageProvider(String accessKey, String secretKey) {
        super(accessKey, secretKey);
    }

    public SnapshotStorageProvider(String accessKey, String secretKey,
                                   Map<String, String> options) {
        super(accessKey, secretKey, options);
    }

    public SnapshotStorageProvider(AmazonS3Client s3Client, String accessKey,
                                   Map<String, String> options) {
        super(s3Client, accessKey, options);
    }

    @Override
    protected StoragePolicy getStoragePolicy() {
        return null; // no transition policy, leaving content in S3 standard
    }
}
