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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import static org.duracloud.common.util.SerializationUtil.deserializeMap;
import static org.duracloud.common.util.SerializationUtil.serializeMap;
import static org.duracloud.storage.domain.HadoopTypes.RUN_HADOOP_TASK_NAME;
import static org.duracloud.storage.domain.HadoopTypes.TASK_OUTPUTS;
import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

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

    private boolean complete = false;
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

    protected abstract String getHadoopJarPrefix();

    @Override
    public void run() {
        try {
            String jarContentId = copyResourceToStorage(getHadoopJarPrefix());
            taskParams.put(TASK_PARAMS.JAR_CONTENT_ID.name(), jarContentId);

            String response = contentStore.performTask(RUN_HADOOP_TASK_NAME,
                                                       serializeMap(taskParams));

            jobId = deserializeMap(response).get(TASK_OUTPUTS.JOB_FLOW_ID.name());

        } catch (Exception e) {
            log.error("Error starting hadoop job " + e.getMessage(), e);
            error = e.getMessage();
        }
        complete = true;
    }

    @Override
    public boolean isComplete() {
        return complete;
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
        complete = true;
    }

    private String copyResourceToStorage(String resourceNamePrefix)
        throws FileNotFoundException, ContentStoreException {
        File workDir = new File(serviceWorkDir);

        File hadoopJar = null;
        for (File file : workDir.listFiles()) {
            if (file.getName().equals(resourceNamePrefix + ".hjar")) {
                hadoopJar = file;
            }
        }
        if (null == hadoopJar) {
            throw new RuntimeException("Unable to find service resources.");
        }

        String hadoopJarContentId = resourceNamePrefix + ".jar";
        contentStore.addContent(workSpaceId,
                                hadoopJarContentId,
                                new FileInputStream(hadoopJar),
                                hadoopJar.length(),
                                "application/java-archive",
                                null,
                                null);

        return hadoopJarContentId;
    }

}
