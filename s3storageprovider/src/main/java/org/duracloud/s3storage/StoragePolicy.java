/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.StorageClass;

/**
 * Defines the parameters of a bucket lifecycle policy, used to transition
 * content from S3 standard storage to another storage class.
 *
 * @author Bill Branan
 *         Date: 9/25/2015
 */
public class StoragePolicy {

    private StorageClass storageClass;
    private int daysToTransition;

    public StoragePolicy(StorageClass storageClass,
                         int daysToTransition) {
        this.storageClass = storageClass;
        this.daysToTransition = daysToTransition;
    }

    public StorageClass getStorageClass() {
        return storageClass;
    }

    public int getDaysToTransition() {
        return daysToTransition;
    }

    public BucketLifecycleConfiguration getBucketLifecycleConfig() {
        // Define the transition to another storage class
        BucketLifecycleConfiguration.Transition transition =
            new BucketLifecycleConfiguration.Transition()
                .withDays(daysToTransition)
                .withStorageClass(storageClass);

        String policyName = "Transition to " + storageClass.name() +
                            " in " + daysToTransition + " days";

        // Use the transition in a rule
        BucketLifecycleConfiguration.Rule rule =
            new BucketLifecycleConfiguration.Rule()
                .withId(policyName)
                .withPrefix("")
                .withStatus(BucketLifecycleConfiguration.ENABLED.toString());
        rule.addTransition(transition);

        return new BucketLifecycleConfiguration().withRules(rule);
    }
}
