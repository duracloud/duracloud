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
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3task.hadoop.param.BulkImageConversionTaskHelper;
import org.duracloud.s3task.hadoop.param.HadoopTaskHelper;
import org.duracloud.s3task.hadoop.param.ReplicationOnDemandTaskHelper;
import org.duracloud.storage.domain.HadoopTypes;
import org.duracloud.storage.provider.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.INSTANCES;
import static org.duracloud.storage.domain.HadoopTypes.JOB_TYPES.BULK_IMAGE_CONVERSION;
import static org.duracloud.storage.domain.HadoopTypes.JOB_TYPES.REP_ON_DEMAND;
import static org.duracloud.storage.domain.HadoopTypes.TASK_OUTPUTS;
import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * @author: Bill Branan
 * Date: May 21, 2010
 */
public class RunHadoopJobTaskRunner  implements TaskRunner {

    private final Logger log =
        LoggerFactory.getLogger(RunHadoopJobTaskRunner.class);

    private static final Integer DEFAULT_NUM_INSTANCES = new Integer(1);
    private static final String DEFAULT_INSTANCE_TYPE = INSTANCES.SMALL.getId();
    private static final Integer DEFAULT_NUM_MAPPERS = new Integer(1);

    private static final String CONFIG_HADOOP_BOOTSTRAP_ACTION =
        "s3://elasticmapreduce/bootstrap-actions/configure-hadoop";

    private S3StorageProvider s3Provider;
    private AmazonS3Client s3Client;
    private AmazonElasticMapReduceClient emrClient;

    public RunHadoopJobTaskRunner(S3StorageProvider s3Provider,
                                  AmazonS3Client s3Client,
                                  AmazonElasticMapReduceClient emrClient) {
        this.s3Provider = s3Provider;
        this.s3Client = s3Client;
        this.emrClient = emrClient;
    }

    public String getName() {
        return HadoopTypes.RUN_HADOOP_TASK_NAME;
    }

    // Run Hadoop Job
    public String performTask(String taskParameters) {
        Map<String, String> taskParams =
            SerializationUtil.deserializeMap(taskParameters);
        String jobType = taskParams.get(TASK_PARAMS.JOB_TYPE.name());
        String workSpaceId = taskParams.get(TASK_PARAMS.WORKSPACE_ID.name());
        String bootstrapContentId = taskParams.get(TASK_PARAMS.BOOTSTRAP_CONTENT_ID.name());
        String jarContentId = taskParams.get(TASK_PARAMS.JAR_CONTENT_ID.name());
        String sourceSpaceId = taskParams.get(TASK_PARAMS.SOURCE_SPACE_ID.name());
        String destSpaceId = taskParams.get(TASK_PARAMS.DEST_SPACE_ID.name());
        String instanceType = taskParams.get(TASK_PARAMS.INSTANCE_TYPE.name());
        String numInstances = taskParams.get(TASK_PARAMS.NUM_INSTANCES.name());
        String mappersPerInstance = taskParams.get(TASK_PARAMS.MAPPERS_PER_INSTANCE.name());
        String dcHost = taskParams.get(TASK_PARAMS.DC_HOST.name());
        String dcPort = taskParams.get(TASK_PARAMS.DC_PORT.name());
        String dcContext = taskParams.get(TASK_PARAMS.DC_CONTEXT.name());
        String dcStoreId = taskParams.get(TASK_PARAMS.DC_STORE_ID.name());
        String dcUsername = taskParams.get(TASK_PARAMS.DC_USERNAME.name());
        String dcPassword = taskParams.get(TASK_PARAMS.DC_PASSWORD.name());

        log.info("Performing " + HadoopTypes.RUN_HADOOP_TASK_NAME +
                 " with the following parameters:" +
                 " jobType=" + jobType +
                 " workSpaceId=" + workSpaceId +
                 " bootstrapContentId=" + bootstrapContentId +
                 " jarContentId=" + jarContentId +
                 " sourceSpaceId=" + sourceSpaceId +
                 " destSpaceId=" + destSpaceId +
                 " instanceType=" + instanceType +
                 " numInstances=" + numInstances +
                 " mappersPerInstance=" + mappersPerInstance +
                 " dcHost=" + dcHost +
                 " dcPort=" + dcPort +
                 " dcContext=" + dcContext +
                 " dcStoreId=" + dcStoreId +
                 " dcUsername=" + dcUsername);

        // Verify a known job type
        HadoopTaskHelper taskHelper = null;
        if(jobType != null && jobType.equals(BULK_IMAGE_CONVERSION.name())) {
            taskHelper = new BulkImageConversionTaskHelper();
        } else if(jobType != null && jobType.equals(REP_ON_DEMAND.name())) {
            taskHelper = new ReplicationOnDemandTaskHelper();
        } else {
            log.info("No TaskHelper for Job Type: "+ jobType);
        }

        // Verify required params were provided
        if (workSpaceId == null || sourceSpaceId == null ||
            destSpaceId == null || jarContentId == null || dcHost == null ||
            dcUsername == null || dcPassword == null) {
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
        jarParams.add(TASK_PARAMS.INPUT_PATH.getCliForm());
        jarParams.add(inputPath);
        jarParams.add(TASK_PARAMS.OUTPUT_PATH.getCliForm());
        jarParams.add(outputPath);

        jarParams.add(TASK_PARAMS.DC_HOST.getCliForm());
        jarParams.add(dcHost);
        if (dcPort != null && !dcPort.equals("")) {
            jarParams.add(TASK_PARAMS.DC_PORT.getCliForm());
            jarParams.add(dcPort);
        }
        if (dcContext != null && !dcContext.equals("")) {
            jarParams.add(TASK_PARAMS.DC_CONTEXT.getCliForm());
            jarParams.add(dcContext);
        }
        if (dcStoreId != null && !dcStoreId.equals("")) {
            jarParams.add(TASK_PARAMS.DC_STORE_ID.getCliForm());
            jarParams.add(dcStoreId);
        }
        jarParams.add(TASK_PARAMS.DC_USERNAME.getCliForm());
        jarParams.add(dcUsername);
        jarParams.add(TASK_PARAMS.DC_PASSWORD.getCliForm());
        jarParams.add(dcPassword);

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
        String jobFlowId = runHadoopJob(jobType,
                                        jarPath,
                                        customBootstrapPath,
                                        logPath,
                                        jarParams,
                                        numberOfInstances,
                                        instanceType,
                                        numMappersPerInstance);

        // Return results
        Map<String, String> returnInfo = new HashMap<String, String>();
        returnInfo.put(TASK_OUTPUTS.RESULTS.name(), "success");
        returnInfo.put(TASK_OUTPUTS.JOB_FLOW_ID.name(), jobFlowId);
        String toReturn = SerializationUtil.serializeMap(returnInfo);
        log.debug("Result of " + HadoopTypes.RUN_HADOOP_TASK_NAME + " task: " + toReturn);
        return toReturn;
    }

    private String runHadoopJob(String jobType,
                                String jarPath,
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

        jobFlow.setName("DuraCloud Job: " + jobType);
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
