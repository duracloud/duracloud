/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.amazonmapreduce.impl.HadoopJobCompletionMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static org.duracloud.common.util.SerializationUtil.deserializeMap;
import static org.duracloud.common.util.SerializationUtil.serializeMap;
import static org.duracloud.storage.domain.HadoopTypes.DESCRIBE_JOB_TASK_NAME;
import static org.duracloud.storage.domain.HadoopTypes.RUN_HADOOP_TASK_NAME;
import static org.duracloud.storage.domain.HadoopTypes.STOP_JOB_TASK_NAME;
import static org.duracloud.storage.domain.HadoopTypes.TASK_OUTPUTS;

/**
 * This class manages starting a hadoop job that relies on a jar being installed
 * within the storageprovider environment.
 *
 * @author: Andrew Woods
 * Date: Sept 29, 2010
 */
public abstract class BaseAmazonMapReduceJobWorker implements AmazonMapReduceJobWorker {

    private final Logger log = LoggerFactory.getLogger(
        BaseAmazonMapReduceJobWorker.class);

    private ContentStore contentStore;
    private String workSpaceId;
    private Map<String, String> taskParams;
    private String serviceWorkDir;

    private JobStatus status = JobStatus.STARTING;
    private String jobId = null;
    private String error = null;

    public BaseAmazonMapReduceJobWorker(ContentStore contentStore,
                                        String workSpaceId,
                                        Map<String, String> taskParams,
                                        String serviceWorkDir) {
        this.contentStore = contentStore;
        this.workSpaceId = workSpaceId;
        this.taskParams = taskParams;
        this.serviceWorkDir = serviceWorkDir;
    }

    /**
     * This abstract method creates and returns a map of filenames that should
     * be found in the service package, and their associated
     * HadoopTypes.TASK_PARAM.name().
     * <p/>
     * Note: jars used by the hadoop framework must have the extension = .hjar
     * so that the services-osgi-framework does not try to deploy the hadoop
     * jar as a service.
     * While copying the .hjar over to the storageprovider, the extension is
     * then corrected to .jar .
     *
     * @return map of resource filename => task param name
     */
    protected abstract Map<String, String> getParamToResourceFileMap();

    @Override
    public void run() {
        try {
            Map<String, String> paramToFileMap = getParamToResourceFileMap();
            for (String param : paramToFileMap.keySet()) {
                String filename = paramToFileMap.get(param);
                String contentId = copyResourceToStorage(filename);
                taskParams.put(param, contentId);
            }

            String response = contentStore.performTask(RUN_HADOOP_TASK_NAME,
                                                       serializeMap(taskParams));

            jobId = deserializeMap(response).get(TASK_OUTPUTS.JOB_FLOW_ID.name());

        } catch (Exception e) {
            log.error("Error starting hadoop job " + e.getMessage(), e);
            error = e.getMessage();
        }
        status = JobStatus.RUNNING;

        new Thread(new HadoopJobCompletionMonitor(this)).start();
    }

    private String copyResourceToStorage(String resourceName)
        throws FileNotFoundException, ContentStoreException {
        File workDir = new File(serviceWorkDir);

        File resourceFile = null;
        for (File file : workDir.listFiles()) {
            if (file.getName().equals(resourceName)) {
                resourceFile = file;
            }
        }
        if (null == resourceFile) {
            throw new RuntimeException(
                "Unable to find service resource: " + resourceName);
        }

        String contentId = resourceName;
        if (resourceName.endsWith(".hjar")) {
            contentId = resourceName.replace(".hjar", ".jar");
        }

        createSpace();

        contentStore.addContent(workSpaceId,
                                contentId,
                                new FileInputStream(resourceFile),
                                resourceFile.length(),
                                "application/java-archive",
                                null,
                                null);

        return contentId;
    }

    private void createSpace() {
        if (null != workSpaceId) {
            try {
                contentStore.createSpace(workSpaceId);
            } catch (ContentStoreException e) {
                log.debug("Ensuring output space exists: " + e.getMessage());
            }
        }
    }

    @Override
    public JobStatus getJobStatus() {
        return status;
    }

    @Override
    public String getJobId() {
        return jobId;
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    public void shutdown() {
        log.info("shutting down: {}", this.getClass().getName());
        if (!isHadoopJobFinished() && jobId != null) {
            String result = null;
            try {
                result = contentStore.performTask(STOP_JOB_TASK_NAME, jobId);

            } catch (ContentStoreException e) {
                log.warn("Error stopping task. JobId {}, class {}",
                         jobId,
                         this.getClass().getName());
            }

            log.info("Stop task, jobId: {}, result: {}, for {}",
                     new Object[]{jobId, result, getClass().getName()});
        }
        status = JobStatus.COMPLETE;
    }

    private boolean isHadoopJobFinished() {
        Map<String, String> map = getJobDetailsMap();
        for (String key : map.keySet()) {
            if (key.equals("Job State")) {
                String state = map.get(key);
                return ("COMPLETED".equals(state) || "FAILED".equals(state));
            }
        }
        return false;
    }

    @Override
    public Map<String, String> getJobDetailsMap() {
        Map<String, String> statusMap = new HashMap<String, String>();

        String jobStatus = describeJob(jobId);
        if (jobStatus != null) {
            statusMap = deserializeMap(jobStatus);

        } else {
            statusMap.put("Error", "Unable to retrieve job status");
        }

        return statusMap;
    }

    private String describeJob(String jobId) {
        String jobStatus = null;
        try {
            jobStatus = contentStore.performTask(DESCRIBE_JOB_TASK_NAME, jobId);

        } catch (ContentStoreException e) {
            log.error("Error Retrieving Job Status", e.getMessage());
        }

        return jobStatus;
    }

}
