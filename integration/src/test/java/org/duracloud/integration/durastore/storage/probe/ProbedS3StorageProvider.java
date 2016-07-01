/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.durastore.storage.probe;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.ProbedStorageProvider;

/**
 * This class implements the StorageProvider interface using a Metrics-Probed
 * S3Service as the underlying storage service.
 *
 * @author Andrew Woods
 */
public class ProbedS3StorageProvider
        extends ProbedStorageProvider {

    private ProbedRestS3Client probedCore;

    public ProbedS3StorageProvider(String accessKey, String secretKey)
            throws StorageException {
        AWSCredentials awsCredentials =
                new BasicAWSCredentials(accessKey, secretKey);

        try {
            probedCore = new ProbedRestS3Client(awsCredentials);
        } catch (AmazonServiceException e) {
            String err =
                    "Could not create connection to S3 due to error: "
                            + e.getMessage();
            throw new StorageException(err, e);
        }

        storageProvider = new S3StorageProvider(probedCore, accessKey, null);
    }

    @Override
    protected MetricsProbed getProbedCore() {
        return probedCore;
    }

}
