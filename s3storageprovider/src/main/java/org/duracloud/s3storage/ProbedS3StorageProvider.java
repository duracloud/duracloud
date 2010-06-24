/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.ProbedStorageProvider;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.security.AWSCredentials;

/**
 * This class implements the StorageProvider interface using a Metrics-Probed
 * S3Service as the underlying storage service.
 *
 * @author Andrew Woods
 */
public class ProbedS3StorageProvider
        extends ProbedStorageProvider {

    private ProbedRestS3Service probedCore;

    public ProbedS3StorageProvider(String accessKey, String secretKey)
            throws StorageException {
        AWSCredentials awsCredentials =
                new AWSCredentials(accessKey, secretKey);

        try {
            probedCore = new ProbedRestS3Service(awsCredentials);
        } catch (S3ServiceException e) {
            String err =
                    "Could not create connection to S3 due to error: "
                            + e.getMessage();
            throw new StorageException(err, e);
        }

        storageProvider = new S3StorageProvider(probedCore, accessKey);
    }

    @Override
    protected MetricsProbed getProbedCore() {
        return probedCore;
    }

}
