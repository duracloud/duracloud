/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.hadoop;

import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.BootstrapActionConfig;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.JobFlowInstancesConfig;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;
import com.amazonaws.services.elasticmapreduce.model.ScriptBootstrapActionConfig;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.provider.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: May 21, 2010
 */
public class RunHadoopJobTaskRunner  implements TaskRunner {

    private final Logger log =
        LoggerFactory.getLogger(RunHadoopJobTaskRunner.class);

    private static final Integer DEFAULT_NUM_INSTANCES = new Integer(1);
    private static final String DEFAULT_INSTANCE_TYPE = "m1.small";
    private static final Integer DEFAULT_NUM_MAPPERS = new Integer(1);

    private static final String CONFIG_HADOOP_BOOTSTRAP_ACTION =
        "s3://elasticmapreduce/bootstrap-actions/configure-hadoop";

    private S3StorageProvider s3Provider;
    private AmazonS3Client s3Client;
    private AmazonElasticMapReduceClient emrClient;

    private static final String TASK_NAME = "run-hadoop-job";

    private static final String IMG_CONV_JOB_TYPE = "bulk-image-conversion";

    public RunHadoopJobTaskRunner(S3StorageProvider s3Provider,
                                  AmazonS3Client s3Client,
                                  AmazonElasticMapReduceClient emrClient) {
        this.s3Provider = s3Provider;
        this.s3Client = s3Client;
        this.emrClient = emrClient;
    }

    public String getName() {
        return TASK_NAME;
    }

    // Run Hadoop Job
    public String performTask(String taskParameters) {
        Map<String, String> taskParams =
            SerializationUtil.deserializeMap(taskParameters);
        String jobType = taskParams.get("jobType");
        String workSpaceId = taskParams.get("workSpaceId");
        String bootstrapContentId = taskParams.get("bootstrapContentId");
        String jarContentId = taskParams.get("jarContentId");
        String sourceSpaceId = taskParams.get("sourceSpaceId");
        String destSpaceId = taskParams.get("destSpaceId");
        String instanceType = taskParams.get("instanceType");
        String numInstances = taskParams.get("numInstances");
        String mappersPerInstance = taskParams.get("mappersPerInstance");

        log.info("Performing " + TASK_NAME +
                 " with the following parameters:" +
                 " jobType=" + jobType +
                 " workSpaceId=" + workSpaceId +
                 " bootstrapContentId=" + bootstrapContentId +
                 " jarContentId=" + jarContentId +
                 " sourceSpaceId=" + sourceSpaceId +
                 " destSpaceId=" + destSpaceId +
                 " instanceType=" + instanceType +
                 " numInstances=" + numInstances +
                 " mappersPerInstance=" + mappersPerInstance);

        // Verify a known job type
        HadoopTaskHelper taskHelper = null;
        if(jobType != null && jobType.equals(IMG_CONV_JOB_TYPE)) {
            taskHelper = new BulkImageConversionTaskHelper();
        } else {
            throw new RuntimeException("Unknown Job Type: " + jobType);
        }

        // Verify required params were provided
        if(workSpaceId == null || sourceSpaceId == null ||
           destSpaceId == null || jarContentId == null)
        {
            throw new RuntimeException("All required parameters not provided");
        }

        // Check instance type
        if(instanceType == null) {
            instanceType = DEFAULT_INSTANCE_TYPE;
        }

        // Check number of instances
        Integer numberOfInstances = DEFAULT_NUM_INSTANCES;
        if(numInstances != null) {
            try {
                numberOfInstances = Integer.valueOf(numInstances);
            } catch(NumberFormatException e) {
                log.warn("Value for numInstances if not a valid integer, " +
                         "using default value " + DEFAULT_NUM_INSTANCES +
                         " instead");
                numberOfInstances = DEFAULT_NUM_INSTANCES;
            }
        }

        // Check number of mappers per instance
        Integer numMappersPerInstance = DEFAULT_NUM_MAPPERS;
        if(mappersPerInstance != null) {
            try {
                numMappersPerInstance = Integer.valueOf(mappersPerInstance);
            } catch(NumberFormatException e) {
                log.warn("Value for mappersPerInstance if not a valid " +
                         "integer, using default value " + DEFAULT_NUM_MAPPERS +
                         " instead");
                numMappersPerInstance = DEFAULT_NUM_MAPPERS;
            }
        }

        // Verify buckets exist        
        String workBucketName = s3Provider.getBucketName(workSpaceId);
        String sourceBucketName = s3Provider.getBucketName(sourceSpaceId);
        String destBucketName = s3Provider.getBucketName(destSpaceId);

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

        // Set custom bootstrap path if necessary
        boolean includeCustomBootstrap = false;
        if(bootstrapContentId != null) {
            ObjectMetadata bootstrapMeta =
                s3Client.getObjectMetadata(workBucketName, bootstrapContentId);
            if(bootstrapMeta != null &&
               !bootstrapMeta.getRawMetadata().isEmpty()) {
                includeCustomBootstrap = true;
            }
        }

        String customBootstrapPath = null;
        if(includeCustomBootstrap) {
            customBootstrapPath =
                "s3n://" + workBucketName + "/" + bootstrapContentId;
        }

        // Set jar and log paths
        String jarPath = "s3n://" + workBucketName + "/" + jarContentId;
        String logPath = "s3n://" + workBucketName + "/logs";

        // Create jar input
        String inputPath = "s3n://" + sourceBucketName + "/";
        String outputPath = "s3n://" + destBucketName + "/";

        List<String> jarParams = new ArrayList<String>();
        jarParams.add("-i");
        jarParams.add(inputPath);
        jarParams.add("-o");
        jarParams.add(outputPath);

        // Add job-type specific jar parameters
        if(taskHelper != null) {
            jarParams = taskHelper.completeJarParams(taskParams, jarParams);
        }

        String jarParamListing =
            Arrays.toString(jarParams.toArray(new String[jarParams.size()]));
        log.info("Running Hadoop Job with parameters:" +
                 " jar-path=" + jarPath +
                 " include-custom-bootstrap=" + includeCustomBootstrap +
                 " custom-bootstrap-path=" + customBootstrapPath +
                 " log-path=" + logPath +
                 " jar-params=" + jarParamListing +
                 " num-instances=" + numberOfInstances +
                 " instance-type=" + instanceType +
                 " num-mappers-per-instance=" + numMappersPerInstance);

        // Run hadoop job
        String jobFlowId = runHadoopJob(jarPath,
                                        customBootstrapPath,
                                        logPath,
                                        jarParams,
                                        numberOfInstances,
                                        instanceType,
                                        numMappersPerInstance);

        // Return results
        Map<String, String> returnInfo = new HashMap<String, String>();
        returnInfo.put("results", "success");
        returnInfo.put("jobFlowId", jobFlowId);
        String toReturn = SerializationUtil.serializeMap(returnInfo);
        log.debug("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }

    private String runHadoopJob(String jarPath,
                                String customBootstrapPath,
                                String logPath,
                                List<String> jarParams,
                                Integer numInstances,
                                String instanceType,
                                Integer numMappersPerInstance) {
        RunJobFlowRequest jobFlow = new RunJobFlowRequest();

        // Create bootstrap actions
        List<BootstrapActionConfig> bootstrapActions =
            new ArrayList<BootstrapActionConfig>();

        // Add number of mappers per instance bootstrap config
        List<String> setMappersArgs = new ArrayList<String>();
        setMappersArgs.add("-s");
        setMappersArgs.add("mapred.tasktracker.map.tasks.maximum=" +
                           numMappersPerInstance);
        BootstrapActionConfig mappersBootstrapConfig =
            createBootstrapAction("Set Hadoop Config",
                                  CONFIG_HADOOP_BOOTSTRAP_ACTION,
                                  setMappersArgs);
        bootstrapActions.add(mappersBootstrapConfig);

        // Add custom bootstrap path if included
        if(customBootstrapPath != null) {
            BootstrapActionConfig customBootstrapConfig =
                createBootstrapAction("Custom BootStrap Action",
                                      customBootstrapPath,
                                      null);
            bootstrapActions.add(customBootstrapConfig);
        }
        jobFlow.setBootstrapActions(bootstrapActions);

        // Set instance config
        JobFlowInstancesConfig instancesConfig = new JobFlowInstancesConfig();
        instancesConfig.setInstanceCount(numInstances);
        instancesConfig.setMasterInstanceType(instanceType);
        instancesConfig.setSlaveInstanceType(instanceType);
        instancesConfig.setHadoopVersion("0.20");
        jobFlow.setInstances(instancesConfig);

        // Set job steps
        HadoopJarStepConfig jarStepConfig = new HadoopJarStepConfig();
        jarStepConfig.setJar(jarPath);
        jarStepConfig.setArgs(jarParams);
        StepConfig stepConfig = new StepConfig();
        stepConfig.setName("Run DuraCloud Service Jar");
        stepConfig.setHadoopJarStep(jarStepConfig);
        jobFlow = jobFlow.withSteps(stepConfig);

        jobFlow.setName("DuraCloud Service Job Flow");
        jobFlow.setLogUri(logPath);

        RunJobFlowResult result = emrClient.runJobFlow(jobFlow);
        return result.getJobFlowId();
    }

    private BootstrapActionConfig createBootstrapAction(String bootstrapName,
                                                        String bootstrapPath,
                                                        List<String> args) {
        ScriptBootstrapActionConfig bootstrapScriptConfig =
            new ScriptBootstrapActionConfig();
        bootstrapScriptConfig.setPath(bootstrapPath);

        if(args != null) {
            bootstrapScriptConfig.setArgs(args);
        }

        BootstrapActionConfig bootstrapConfig = new BootstrapActionConfig();
        bootstrapConfig.setName(bootstrapName);
        bootstrapConfig.setScriptBootstrapAction(bootstrapScriptConfig);

        return bootstrapConfig;
    }


}
