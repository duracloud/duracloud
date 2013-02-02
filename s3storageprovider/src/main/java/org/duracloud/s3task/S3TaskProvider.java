/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.s3storage.S3ProviderUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3task.hadoop.DescribeHadoopJobTaskRunner;
import org.duracloud.s3task.hadoop.RunHadoopJobTaskRunner;
import org.duracloud.s3task.hadoop.StopHadoopJobTaskRunner;
import org.duracloud.s3task.storage.SetReducedStorageTaskRunner;
import org.duracloud.s3task.storage.SetStandardStorageTaskRunner;
import org.duracloud.s3task.streaming.AddStreamingItemTaskRunner;
import org.duracloud.s3task.streaming.DeleteStreamingTaskRunner;
import org.duracloud.s3task.streaming.DisableStreamingTaskRunner;
import org.duracloud.s3task.streaming.EnableStreamingTaskRunner;
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

    public S3TaskProvider(String accessKey, String secretKey) {
        log = LoggerFactory.getLogger(S3TaskProvider.class);

        S3StorageProvider s3Provider =
            new S3StorageProvider(accessKey, secretKey);
        CloudFrontService cfService =
            S3ProviderUtil.getCloudFrontService(accessKey, secretKey);

        AmazonS3Client s3Client =
            S3ProviderUtil.getAmazonS3Client(accessKey, secretKey);
        AmazonElasticMapReduceClient emrClient =
            S3ProviderUtil.getAmazonEMRClient(accessKey, secretKey);

        taskList.add(new NoopTaskRunner());
        taskList.add(new EnableStreamingTaskRunner(s3Provider,
                                                   s3Client,
                                                   cfService));
        taskList.add(new AddStreamingItemTaskRunner(s3Provider,
                                                    s3Client,
                                                    cfService));
        taskList.add(new DisableStreamingTaskRunner(s3Provider,
                                                    s3Client,
                                                    cfService));
        taskList.add(new DeleteStreamingTaskRunner(s3Provider,
                                                   s3Client,
                                                   cfService));
        taskList.add(new RunHadoopJobTaskRunner(s3Provider,
                                                s3Client,
                                                emrClient));
        taskList.add(new DescribeHadoopJobTaskRunner(emrClient));
        taskList.add(new StopHadoopJobTaskRunner(emrClient));
        taskList.add(new SetStandardStorageTaskRunner(s3Provider, s3Client));
        taskList.add(new SetReducedStorageTaskRunner(s3Provider, s3Client));
    }

}
