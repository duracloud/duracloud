/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3task.storage.SetReducedStorageTaskRunner;
import org.duracloud.s3task.storage.SetStandardStorageTaskRunner;
import org.duracloud.s3task.streaming.DeleteStreamingTaskRunner;
import org.duracloud.s3task.streaming.DisableStreamingTaskRunner;
import org.duracloud.s3task.streaming.EnableStreamingTaskRunner;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskProviderBase;
import org.jets3t.service.CloudFrontService;
import org.slf4j.LoggerFactory;

/**
 * Handles tasks specific to content stored in Amazon S3 
 *
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class S3TaskProvider extends TaskProviderBase {

    public S3TaskProvider(StorageProvider s3Provider,
                          S3StorageProvider unwrappedS3Provider,
                          AmazonS3Client s3Client,
                          CloudFrontService cfService) {
        log = LoggerFactory.getLogger(S3TaskProvider.class);

        taskList.add(new NoopTaskRunner());
        taskList.add(new EnableStreamingTaskRunner(s3Provider,
                                                   unwrappedS3Provider,
                                                   s3Client,
                                                   cfService));
        taskList.add(new DisableStreamingTaskRunner(s3Provider,
                                                    unwrappedS3Provider,
                                                    s3Client,
                                                    cfService));
        taskList.add(new DeleteStreamingTaskRunner(s3Provider,
                                                   unwrappedS3Provider,
                                                   s3Client,
                                                   cfService));
        taskList.add(new SetStandardStorageTaskRunner(s3Provider,
                                                      unwrappedS3Provider,
                                                      s3Client));
        taskList.add(new SetReducedStorageTaskRunner(s3Provider,
                                                     unwrappedS3Provider,
                                                     s3Client));
    }

}
