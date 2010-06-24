/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import org.duracloud.s3storage.S3ProviderUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.duracloud.storage.provider.TaskProvider;
import org.duracloud.storage.provider.TaskRunner;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles tasks specific to content stored in Amazon S3 
 *
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class S3TaskProvider implements TaskProvider {

    private final Logger log = LoggerFactory.getLogger(S3TaskProvider.class);

    private List<TaskRunner> taskList = new ArrayList<TaskRunner>();

    public S3TaskProvider(String accessKey, String secretKey) {
        S3Service s3Service =
            S3ProviderUtil.getS3Service(accessKey, secretKey);
        S3StorageProvider s3Provider =
            new S3StorageProvider(accessKey, secretKey);
        CloudFrontService cfService =
            S3ProviderUtil.getCloudFrontService(accessKey, secretKey);

        taskList.add(new NoopTaskRunner());
        taskList.add(new EnableStreamingTaskRunner(s3Provider,
                                                   s3Service,
                                                   cfService));
        taskList.add(new DisableStreamingTaskRunner(s3Provider,
                                                    s3Service,
                                                    cfService));
        taskList.add(new DeleteStreamingTaskRunner(s3Provider,
                                                   s3Service,
                                                   cfService));        
    }

    public List<String> getSupportedTasks() {
        log.debug("getSupportedTasks()");

        List<String> supportedTasks = new ArrayList<String>();
        for(TaskRunner runner : taskList) {
            supportedTasks.add(runner.getName());
        }
        return supportedTasks;
    }

    public String performTask(String taskName, String taskParameters) {
        log.debug("performTask(" + taskName + ", " + taskParameters + ")");

        for(TaskRunner runner : taskList) {
            if(runner.getName().equals(taskName)) {
                return runner.performTask(taskParameters);
            }
        }
        throw new UnsupportedTaskException(taskName);
    }
    
}
