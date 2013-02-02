/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.glaciertask;

import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.glacierstorage.GlacierStorageProvider;
import org.duracloud.s3storage.S3ProviderUtil;
import org.duracloud.storage.provider.TaskProviderBase;
import org.slf4j.LoggerFactory;

/**
 * @author: Bill Branan
 * Date: 2/1/13
 */
public class GlacierTaskProvider extends TaskProviderBase {

    public GlacierTaskProvider(String accessKey, String secretKey) {
        log = LoggerFactory.getLogger(GlacierTaskProvider.class);

        AmazonS3Client s3Client =
            S3ProviderUtil.getAmazonS3Client(accessKey, secretKey);
        GlacierStorageProvider glacierProvider =
            new GlacierStorageProvider(s3Client, accessKey);

        taskList.add(new RestoreContentTaskRunner(glacierProvider, s3Client));
    }

}
