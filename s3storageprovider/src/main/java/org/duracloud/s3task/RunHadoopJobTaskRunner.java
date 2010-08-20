/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.provider.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: May 21, 2010
 */
public class RunHadoopJobTaskRunner  implements TaskRunner {

    private final Logger log = LoggerFactory.getLogger(RunHadoopJobTaskRunner.class);

    private S3StorageProvider s3Provider;
    private AmazonS3Client s3Client;

    private static final String TASK_NAME = "run-hadoop-job";

    public RunHadoopJobTaskRunner(S3StorageProvider s3Provider,
                                  AmazonS3Client s3Client) {
        this.s3Provider = s3Provider;
        this.s3Client = s3Client;
    }

    public String getName() {
        return TASK_NAME;
    }

    // Run Hadoop Job
    public String performTask(String taskParameters) {
        Map<String, String> taskParams =
            SerializationUtil.deserializeMap(taskParameters);
        String workSpaceId = taskParams.get("workSpaceId");
        String bootstrapContentId = taskParams.get("bootstrapContentId");
        String jarContentId = taskParams.get("jarContentId");
        String sourceSpaceId = taskParams.get("sourceSpaceId");
        String destSpaceId = taskParams.get("destSpaceId");
        String instanceType = taskParams.get("instanceType");
        String numInstances = taskParams.get("numInstances");

        log.info("Performing " + TASK_NAME +
                 " with the following parameters:" +
                 " workSpaceId=" + workSpaceId +
                 " bootstrapContentId=" + bootstrapContentId +
                 " jarContentId=" + jarContentId +
                 " sourceSpaceId=" + sourceSpaceId +
                 " destSpaceId=" + destSpaceId +
                 " instanceType=" + instanceType +
                 " numInstances=" + numInstances);

        // Verify required params were provided
        if(workSpaceId == null || sourceSpaceId == null ||
           destSpaceId == null || jarContentId == null)
        {
            throw new RuntimeException("All required parameters not provided");
        }

        String workBucketName = s3Provider.getBucketName(workSpaceId);
        String sourceBucketName = s3Provider.getBucketName(sourceSpaceId);
        String destBucketName = s3Provider.getBucketName(destSpaceId);

        // Verify buckets exist
        boolean workExists = s3Client.doesBucketExist(workBucketName);
        boolean sourceExists = s3Client.doesBucketExist(sourceBucketName);
        boolean destExists = s3Client.doesBucketExist(destBucketName);
        if(!workExists || !sourceExists || !destExists) {
            throw new RuntimeException("Source, Destination, and Work " +
                "buckets must exist in order to run a hadoop job");
        }

        // Verify that the destination bucket is empty (except for metadata)
        ObjectListing destListing = s3Client.listObjects(destBucketName);
        if(destListing.getObjectSummaries().size() > 1) {
            throw new RuntimeException("The destination bucket must be empty " +
                                       "in order to run a hadoop job");
        }

        // Verify jar exists
        ObjectMetadata jarMeta =
            s3Client.getObjectMetadata(workBucketName, jarContentId);
        if(jarMeta == null || jarMeta.getRawMetadata().isEmpty()) {
            throw new RuntimeException("Hadoop Jar: " + jarContentId +
                                       " does not exist in bucket " +
                                       workBucketName);
        }

        boolean includeBootstrap = false;
        if(bootstrapContentId != null) {
            ObjectMetadata bootstrapMeta =
                s3Client.getObjectMetadata(workBucketName, bootstrapContentId);
            if(bootstrapMeta != null &&
               !bootstrapMeta.getRawMetadata().isEmpty()) {
                includeBootstrap = true;
            }
        }

        String jarPath = workBucketName + "/" + jarContentId;
        String bootstrapPath =
            "s3://" + workBucketName + "/" + bootstrapContentId;
        String logPath = "s3n://" + workBucketName + "/logs";

        // Create jar input
        String inputPath = "s3n://" + sourceBucketName + "/";
        String outputPath = "s3n://" + destBucketName + "/";

        String jarParams = "-inputPath " + inputPath +
                           " -outputPath " + outputPath;

        // Add image conversion specific jar parameters
        String destFormat = taskParams.get("destFormat");
        String namePrefix = taskParams.get("namePrefix");
        String nameSuffix = taskParams.get("nameSuffix");
        String colorSpace = taskParams.get("colorSpace");

        if(destFormat == null) {
            throw new RuntimeException("Destination format must be provided " +
                                       "to run image conversion hadoop job");
        }

        jarParams += " -destFormat " + destFormat;
        if(namePrefix != null && !namePrefix.equals("")) {
            jarParams += " -namePrefix " + namePrefix;
        }
        if(nameSuffix != null && !nameSuffix.equals("")) {
            jarParams += " -nameSuffix " + nameSuffix;
        }
        if(colorSpace != null && !colorSpace.equals("")) {
            jarParams += " -colorSpace " + colorSpace;
        }

        log.info("Running Hadoop Job:" +
                 " jar-path=" + jarPath +
                 " include-bootstrap=" + includeBootstrap +
                 " bootstrap-path=" + bootstrapPath +
                 " log-path=" + logPath +
                 " jar-params=" + jarParams);

        //TODO: Actually run job
        
        String results = "success";

        // Return results
        Map<String, String> returnInfo = new HashMap<String, String>();
        returnInfo.put("results", results);
        String toReturn = SerializationUtil.serializeMap(returnInfo);
        log.debug("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }

}
