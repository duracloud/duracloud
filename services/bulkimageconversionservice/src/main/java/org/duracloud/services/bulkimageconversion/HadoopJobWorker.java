/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.bulkimageconversion;

import org.duracloud.client.ContentStore;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Aug 18, 2010
 */
public class HadoopJobWorker implements Runnable {

    private final Logger log = LoggerFactory.getLogger(HadoopJobWorker.class);

    private static final String RUN_HADOOP_JOB_TASK = "run-hadoop-job";    

    private ContentStore contentStore;
    private String workSpaceId;
    private Map<String, String> taskParams;
    private String serviceWorkDir;
    
    private boolean complete = false;
    private String error = null;

    public HadoopJobWorker(ContentStore contentStore,
                           String workSpaceId,
                           Map<String, String> taskParams,
                           String serviceWorkDir) {
        this.contentStore = contentStore;
        this.workSpaceId = workSpaceId;
        this.taskParams = taskParams;
        this.serviceWorkDir = serviceWorkDir;
    }

    @Override
    public void run() {
        try {
            String finalParams = moveResourcesToStorage();
            contentStore.performTask(RUN_HADOOP_JOB_TASK, finalParams);
        } catch(Exception e) {
            log.error("Error encountered starting hadoop job " +
                      e.getMessage(), e);
            error = e.getMessage();
        }
        complete = true;
    }

    public boolean isComplete() {
        return complete;
    }

    public String getError() {
        return error;
    }    

    private String moveResourcesToStorage() throws FileNotFoundException,
                                                   ContentStoreException {
        File workDir = new File(serviceWorkDir);

        File hadoopJar = null;
        File bootstrapScript = null;
        for(File file : workDir.listFiles()) {
            if(file.getName().equals("image-conversion-processor.hjar")) {
                hadoopJar = file;
            } else if(file.getName().equals("install-image-magick.sh")) {
                bootstrapScript = file;
            }
        }
        if(hadoopJar == null || bootstrapScript == null) {
            throw new RuntimeException("Unable to find service resources.");
        }

        String hadoopJarContentId = "image-conversion-processor.jar";
        contentStore.addContent(workSpaceId,
                                hadoopJarContentId,
                                new FileInputStream(hadoopJar),
                                hadoopJar.length(),
                                "application/java-archive",
                                null,
                                null);

        String bootstrapScriptContentId = bootstrapScript.getName();
        contentStore.addContent(workSpaceId,
                                bootstrapScriptContentId,
                                new FileInputStream(bootstrapScript),
                                bootstrapScript.length(),
                                "application/x-shellscript",
                                null,
                                null);

        taskParams.put("bootstrapContentId", bootstrapScriptContentId);
        taskParams.put("jarContentId", hadoopJarContentId);

        return SerializationUtil.serializeMap(taskParams);
    }    
}
