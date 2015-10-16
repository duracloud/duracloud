/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.storage;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.StorageClass;
import org.duracloud.StorageTaskConstants;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storage.StoragePolicy;
import org.duracloud.s3storageprovider.dto.SetStoragePolicyTaskParameters;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Task which sets a lifecycle bucket policy for DuraCloud spaces
 * backed by S3 buckets. The bucket policy is set to transition
 * stored content to a given storage class after a set period of
 * time.
 *
 * @author: Bill Branan
 * Date: Sept 25, 2015
 */
public class SetStoragePolicyTaskRunner implements TaskRunner {

    private static final String TASK_NAME =
        StorageTaskConstants.SET_STORAGE_POLICY_TASK_NAME;

    protected S3StorageProvider unwrappedS3Provider;

    public SetStoragePolicyTaskRunner(S3StorageProvider unwrappedS3Provider) {
        this.unwrappedS3Provider = unwrappedS3Provider;
    }

    public String getName() {
        return TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        SetStoragePolicyTaskParameters taskParams =
            SetStoragePolicyTaskParameters.deserialize(taskParameters);

        // Get parameters
        String spaceId = taskParams.getSpaceId();
        int daysToTransition = taskParams.getDaysToTransition();
        StorageClass storageClass;
        try {
             storageClass = StorageClass.fromValue(taskParams.getStorageClass());
        } catch(IllegalArgumentException e) {
            throw new RuntimeException("Cannot set storage policy due to invalid " +
                                       "storage class. The valid storage class " +
                                       "options are: " +
                                       Arrays.asList(StorageClass.values()));
        }

        // Will throw if bucket does not exist
        String bucketName = unwrappedS3Provider.getBucketName(spaceId);

        // Set bucket lifecycle policy
        StoragePolicy storagePolicy = new StoragePolicy(storageClass, daysToTransition);
        unwrappedS3Provider.setSpaceLifecycle(bucketName,
                                              storagePolicy.getBucketLifecycleConfig());

        return "Successfully set storage policy on space " + spaceId +
               " to move content to " + storageClass.name() +
               " after " + daysToTransition + " days";
    }
}
