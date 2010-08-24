/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.bulkimageconversion;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * Service which converts image files from one format to another
 * in a bulk fashion by utilizing hadoop.
 *
 * @author Bill Branan
 *         Date: Aug 17, 2010
 */
public class BulkImageConversionService extends BaseService implements ComputeService, ManagedService {

    private final Logger log = LoggerFactory.getLogger(BulkImageConversionService.class);

    private static final String JOB_TYPE = "bulk-image-conversion";
    private static final String DEFAULT_DURASTORE_HOST = "localhost";
    private static final String DEFAULT_DURASTORE_PORT = "8080";
    private static final String DEFAULT_DURASTORE_CONTEXT = "durastore";
    private static final String DEFAULT_TO_FORMAT = "jp2";
    private static final String DEFAULT_COLORSPACE = "source";
    private static final String DEFAULT_SOURCE_SPACE_ID = "image-conversion-source";
    private static final String DEFAULT_DEST_SPACE_ID = "image-conversion-dest";
    private static final String DEFAULT_WORK_SPACE_ID = "image-conversion-work";
    private static final String DEFAULT_NAME_PREFIX = "";
    private static final String DEFAULT_NAME_SUFFIX = "";
    private static final String DEFAULT_NUM_INSTANCES = "1";
    private static final String DEFAULT_INSTANCE_TYPE = "m1.small";

    private static final String STOP_JOB_TASK = "stop-hadoop-job";
    private static final String DESCRIBE_JOB_TASK = "describe-hadoop-job";
    
    private String duraStoreHost;
    private String duraStorePort;
    private String duraStoreContext;
    private String username;
    private String password;
    private String toFormat;
    private String colorSpace;
    private String sourceSpaceId;
    private String destSpaceId;
    private String workSpaceId;
    private String namePrefix;
    private String nameSuffix;
    private String numInstances;
    private String instanceType;

    private ContentStore contentStore;
    private HadoopJobWorker worker;

    @Override
    public void start() throws Exception {
        log.info("Starting Bulk Image Conversion Service as " + username);
        this.setServiceStatus(ServiceStatus.STARTING);

        // Create content store
        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(duraStoreHost,
                                        duraStorePort,
                                        duraStoreContext);
        storeManager.login(new Credential(username, password));
        contentStore = storeManager.getPrimaryContentStore();

        // Start worker thread
        worker = new HadoopJobWorker(contentStore,
                                     workSpaceId,
                                     collectTaskParams(),
                                     getServiceWorkDir());
        Thread workerThread = new Thread(worker);
        workerThread.start();

        this.setServiceStatus(ServiceStatus.STARTED);        
    }

    protected Map<String, String> collectTaskParams() {
        Map<String, String> taskParams = new HashMap<String, String>();
        
        taskParams.put("jobType", JOB_TYPE);
        taskParams.put("workSpaceId", workSpaceId);
        taskParams.put("sourceSpaceId", sourceSpaceId);
        taskParams.put("destSpaceId", destSpaceId);
        taskParams.put("destFormat", toFormat);
        if(namePrefix != null && !namePrefix.equals("")) {
            taskParams.put("namePrefix", namePrefix);
        }
        if(nameSuffix != null && !namePrefix.equals("")) {
            taskParams.put("nameSuffix", nameSuffix);
        }
        if(colorSpace != null) {
            taskParams.put("colorSpace", colorSpace);
        }
        taskParams.put("instanceType", instanceType);
        taskParams.put("numInstances", numInstances);

        return taskParams;
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping Bulk Image Conversion Service");
        this.setServiceStatus(ServiceStatus.STOPPING);

        // Stop hadoop job
        String jobId = worker.getJobId();
        if(jobId != null) {
            contentStore.performTask(STOP_JOB_TASK, jobId);
        }

        this.setServiceStatus(ServiceStatus.STOPPED);
    }

    @Override
    public Map<String, String> getServiceProps() {
        Map<String, String> props = super.getServiceProps();

        boolean complete = false;
        if(worker != null) {
            complete = worker.isComplete();

            String error = worker.getError();
            if(error != null) {
                props.put("Errors Encountered", error);
            }

            String jobId = worker.getJobId();
            if(jobId != null) {
                props.put("Job ID", jobId);
                String jobStatus = null;
                try {
                    jobStatus =
                        contentStore.performTask(DESCRIBE_JOB_TASK, jobId);
                } catch(ContentStoreException e) {
                    props.put("Error Retrieving Job Status", e.getMessage());    
                }

                if(jobStatus != null) {
                    Map<String, String> jobStatusMap =
                        SerializationUtil.deserializeMap(jobStatus);
                    for(String key : jobStatusMap.keySet()) {
                        props.put(key, jobStatusMap.get(key));
                    }
                }
            }
        }

        if(complete) {
            props.put("Service Status", "Job Started");
        } else {
            props.put("Service Status", "Starting Job...");
        }

        return props;
    }

    @SuppressWarnings("unchecked")
    public void updated(Dictionary config) throws ConfigurationException {
        log("Attempt made to update Image Conversion Service configuration " +
            "via updated method. Updates should occur via class setters.");
    }

    public String getDuraStoreHost() {
        return duraStoreHost;
    }

    public void setDuraStoreHost(String duraStoreHost) {
        if(duraStoreHost != null && !duraStoreHost.equals("") ) {
            this.duraStoreHost = duraStoreHost;
        } else {
            log("Attempt made to set duraStoreHost to " + duraStoreHost +
                ", which is not valid. Setting value to default: " +
                DEFAULT_DURASTORE_HOST);
            this.duraStoreHost = DEFAULT_DURASTORE_HOST;
        }
    }

    public String getDuraStorePort() {
        return duraStorePort;
    }

    public void setDuraStorePort(String duraStorePort) {
        if(duraStorePort != null) {
            this.duraStorePort = duraStorePort;
        } else {
            log("Attempt made to set duraStorePort to null, which is not " +
                "valid. Setting value to default: " + DEFAULT_DURASTORE_PORT);
            this.duraStorePort = DEFAULT_DURASTORE_PORT;
        }
    }

    public String getDuraStoreContext() {
        return duraStoreContext;
    }

    public void setDuraStoreContext(String duraStoreContext) {
        if(duraStoreContext != null && !duraStoreContext.equals("")) {
            this.duraStoreContext = duraStoreContext;
        } else {
            log("Attempt made to set duraStoreContext to null or empty, " +
                "which is not valid. Setting value to default: " +
                DEFAULT_DURASTORE_CONTEXT);
            this.duraStoreContext = DEFAULT_DURASTORE_CONTEXT;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToFormat() {
        return toFormat;
    }

    public void setToFormat(String toFormat) {
        if(toFormat != null && !toFormat.equals("")) {
            this.toFormat = toFormat;
        } else {
            log("Attempt made to set toFormat to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_TO_FORMAT);
            this.toFormat = DEFAULT_TO_FORMAT;
        }
    }

    public String getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(String colorSpace) {
        if(colorSpace != null && !colorSpace.equals("")) {
            this.colorSpace = colorSpace;
        } else {
            log("Attempt made to set colorSpace to null, which is not valid. " +
                "Setting value to default: " + DEFAULT_COLORSPACE);
            this.colorSpace = DEFAULT_COLORSPACE;
        }
    }

    public String getSourceSpaceId() {
        return sourceSpaceId;
    }

    public void setSourceSpaceId(String sourceSpaceId) {
        if(sourceSpaceId != null && !sourceSpaceId.equals("")) {
            this.sourceSpaceId = sourceSpaceId;
        } else {
            log("Attempt made to set sourceSpaceId to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_SOURCE_SPACE_ID);
            this.sourceSpaceId = DEFAULT_SOURCE_SPACE_ID;
        }
    }

    public String getDestSpaceId() {
        return destSpaceId;
    }

    public void setDestSpaceId(String destSpaceId) {
        if(destSpaceId != null && !destSpaceId.equals("")) {
            this.destSpaceId = destSpaceId;
        } else {
            log("Attempt made to set destSpaceId to to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_DEST_SPACE_ID);
            this.destSpaceId = DEFAULT_DEST_SPACE_ID;
        }
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        if(namePrefix != null) {
            this.namePrefix = namePrefix;
        } else {
            log("Attempt made to set namePrefix to null, which is not valid. " +
                "Setting value to default: " + DEFAULT_NAME_PREFIX);
            this.namePrefix = DEFAULT_NAME_PREFIX;
        }
    }

    public String getNameSuffix() {
        return nameSuffix;
    }

    public void setNameSuffix(String nameSuffix) {
        if(nameSuffix != null) {
            this.nameSuffix = nameSuffix;
        } else {
            log("Attempt made to set nameSuffix to null, which is not valid. " +
                "Setting value to default: " + DEFAULT_NAME_SUFFIX);
            this.nameSuffix = DEFAULT_NAME_SUFFIX;
        }
    }

    public String getWorkSpaceId() {
        return workSpaceId;
    }

    public void setWorkSpaceId(String workSpaceId) {        
        if(workSpaceId != null && !workSpaceId.equals("")) {
            this.workSpaceId = workSpaceId;
        } else {
            log("Attempt made to set workSpaceId to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_WORK_SPACE_ID);
            this.workSpaceId = DEFAULT_WORK_SPACE_ID;
        }
    }

    public String getNumInstances() {
        return numInstances;
    }

    public void setNumInstances(String numInstances) {
        if(numInstances != null && !numInstances.equals("")) {
            try {
                Integer.valueOf(numInstances);
                this.numInstances = numInstances;
            } catch(NumberFormatException e) {
                log("Attempt made to set numInstances to a non-numerical " +
                    "value, which is not valid. Setting value to default: " +
                    DEFAULT_NUM_INSTANCES);
                this.numInstances = DEFAULT_NUM_INSTANCES;
            }
        } else {
            log("Attempt made to set numInstances to to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_NUM_INSTANCES);
            this.numInstances = DEFAULT_NUM_INSTANCES;
        }
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {

        if(instanceType != null && !instanceType.equals("")) {
            this.instanceType = instanceType;
        } else {
            log("Attempt made to set typeOfInstance to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_INSTANCE_TYPE);
            this.instanceType = DEFAULT_INSTANCE_TYPE;
        }
    }

    private void log(String logMsg) {
        log.warn(logMsg);
    }
}