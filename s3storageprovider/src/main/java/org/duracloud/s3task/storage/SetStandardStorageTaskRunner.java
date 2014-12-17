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
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.provider.StorageProvider;

/**
 * Sets all content items in a space to use the standard storage class 
 * as provided by Amazon S3.
 *
 * @author: Bill Branan
 * Date: Aug 30, 2010
 */
public class SetStandardStorageTaskRunner extends BaseStorageClassTaskRunner {

    private static final String TASK_NAME = "set-standard-storage-class";

    private static final StorageClass STORAGE_CLASS = StorageClass.Standard;

    public SetStandardStorageTaskRunner(StorageProvider s3Provider,
                                        S3StorageProvider unwrappedS3Provider,
                                        AmazonS3Client s3Client) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.s3Client = s3Client;
    }

    protected StorageClass getStorageClass() {
        return STORAGE_CLASS;
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }

}
