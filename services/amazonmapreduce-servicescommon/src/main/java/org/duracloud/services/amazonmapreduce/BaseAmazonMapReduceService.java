/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.duracloud.services.amazonmapreduce.impl.SimpleJobCompletionMonitor;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import static org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker.JobStatus;
import static org.duracloud.storage.domain.HadoopTypes.INSTANCES.SMALL;
import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * This service contains the base logic common across services leveraging the
 * Amazon elastic map reduce framework.
 * It primarily collects configuration properties and starts task workers.
 *
 * @author Andrew Woods
 *         Date: Sept 29, 2010
 */
public abstract class BaseAmazonMapReduceService extends BaseService implements ComputeService, ManagedService {

    public final Logger log = LoggerFactory.getLogger(
        BaseAmazonMapReduceService.class);

    private static final String DEFAULT_DURASTORE_HOST = "localhost";
    private static final String DEFAULT_DURASTORE_PORT = "8080";
    private static final String DEFAULT_DURASTORE_CONTEXT = "durastore";
    private static final String DEFAULT_SOURCE_SPACE_ID = "service-source";
    protected static final String DEFAULT_NUM_INSTANCES = "1";
    protected static final String DEFAULT_INSTANCE_TYPE = SMALL.getId();
    protected static final String DEFAULT_NUM_MAPPERS = "1";

    protected static final String OPTIMIZE_MODE_STANDARD = "standard";
    protected static final String OPTIMIZE_COST = "optimize_for_cost";
    protected static final String OPTIMIZE_SPEED = "optimize_for_speed";

    private long sleepMillis = 30000;

    private String duraStoreHost;
    private String duraStorePort;
    private String duraStoreContext;
    private String username;
    private String password;
    private String sourceSpaceId;
    private String destSpaceId;
    private String workSpaceId;
    private String mappersPerInstance;

    private String optimizeMode;
    private String optimizeType;
    private String numInstances;
    private String instanceType;
    private String costNumInstances;
    private String costInstanceType;
    private String speedNumInstances;
    private String speedInstanceType;

    private ContentStore contentStore;

    protected abstract AmazonMapReduceJobWorker getJobWorker();

    protected abstract AmazonMapReduceJobWorker getPostJobWorker();

    protected abstract String getJobType();

    protected abstract String getNumMappers(String instanceType);

    @Override
    public void start() throws Exception {
        log.info("Starting " + getServiceId() + " as " + username);
        this.setServiceStatus(ServiceStatus.STARTING);
        super.start();

        AmazonMapReduceJobWorker jobWorker = getJobWorker();
        AmazonMapReduceJobWorker postJobWorker = getPostJobWorker();

        startWorker(jobWorker);
        startWorker(postJobWorker);

        // Signal doneWorking() when service is complete.
        AmazonMapReduceJobWorker worker =
            postJobWorker != null ? postJobWorker : jobWorker;
        new Thread(new SimpleJobCompletionMonitor(worker,
                                                  this,
                                                  sleepMillis)).start();
    }

    private void startWorker(Runnable worker) {
        if (worker != null) {
            new Thread(worker).start();
            log.info("started worker of class: " + worker.getClass());
        }
    }

    protected Map<String, String> collectTaskParams() {
        Map<String, String> taskParams = new HashMap<String, String>();

        taskParams.put(TASK_PARAMS.JOB_TYPE.name(), getJobType());
        taskParams.put(TASK_PARAMS.WORKSPACE_ID.name(), workSpaceId);
        taskParams.put(TASK_PARAMS.SOURCE_SPACE_ID.name(), sourceSpaceId);
        taskParams.put(TASK_PARAMS.DEST_SPACE_ID.name(), destSpaceId);
        taskParams.put(TASK_PARAMS.INSTANCE_TYPE.name(), getInstancesType());
        taskParams.put(TASK_PARAMS.NUM_INSTANCES.name(), getNumOfInstances());

        taskParams.put(TASK_PARAMS.MAPPERS_PER_INSTANCE.name(), getNumMappers(
            getInstancesType()));

        taskParams.put(TASK_PARAMS.DC_HOST.name(), getDuraStoreHost());
        taskParams.put(TASK_PARAMS.DC_PORT.name(), getDuraStorePort());
        taskParams.put(TASK_PARAMS.DC_CONTEXT.name(), getDuraStoreContext());
        taskParams.put(TASK_PARAMS.DC_USERNAME.name(), getUsername());
        taskParams.put(TASK_PARAMS.DC_PASSWORD.name(), getPassword());
        // TODO: we should allow users to specify storageprovider:id
        // taskParams.put(TASK_PARAMS.DC_STORE_ID.name(), getPassword());

        return taskParams;
    }

    public String getInstancesType() {
        String instanceType = getInstanceType();

        if(getOptimizeMode().equals(OPTIMIZE_MODE_STANDARD)) {
            if(getOptimizeType().equals(OPTIMIZE_COST)) {
                instanceType = getCostInstanceType();
            }
            else if(getOptimizeType().equals(OPTIMIZE_SPEED)) {
                instanceType = getSpeedInstanceType();
            }
        }

        return instanceType;
    }

    public String getNumOfInstances() {
        String numOfInstances = getNumInstances();

        if(getOptimizeMode().equals(OPTIMIZE_MODE_STANDARD)) {
            if(getOptimizeType().equals(OPTIMIZE_COST)) {
                numOfInstances = getCostNumInstances();
            }
            else if(getOptimizeType().equals(OPTIMIZE_SPEED)) {
                numOfInstances = getSpeedNumInstances();
            }
        }

        return numOfInstances;
    }

    @Override
    public void stop() throws Exception {
        log.info("stopping {}, {}", getServiceId(), this.getClass().getName());
        this.setServiceStatus(ServiceStatus.STOPPING);

        if (getJobWorker() != null) {
            getJobWorker().shutdown();
        }

        if (getPostJobWorker() != null) {
            getPostJobWorker().shutdown();
        }

        doneWorking();
        this.setServiceStatus(ServiceStatus.STOPPED);
    }

    @Override
    public void doneWorking() {
        AmazonMapReduceJobWorker postWorker = getPostJobWorker();
        if (postWorker != null) {
            collectWorkerProps(postWorker);
        }

        super.doneWorking();
    }

    @Override
    public Map<String, String> getServiceProps() {
        Map<String, String> props = new HashMap<String,String>();

        AmazonMapReduceJobWorker.JobStatus jobStatus = JobStatus.UNKNOWN;
        AmazonMapReduceJobWorker worker = getJobWorker();
        if (worker != null) {
            jobStatus = worker.getJobStatus();
            
            props = collectWorkerProps(worker);
        }

        AmazonMapReduceJobWorker postWorker = getPostJobWorker();
        if (jobStatus.isComplete() && null != postWorker) {
            jobStatus = postWorker.getJobStatus();

            // overwrite jobWorker props as necessary.
            props.putAll(collectWorkerProps(postWorker));
        }

        synchronized (this) { // ensures the status is not set incorrectly
            if (!super.getServiceStatus().isComplete()) {
                if (jobStatus != JobStatus.UNKNOWN) {
                    ServiceStatus serviceStatus = jobStatus.toServiceStatus();
                    super.setServiceStatus(serviceStatus);
                }
            }
        }

        log.info("Job Status, {}: {}",
                 getClass().getName(),
                 jobStatus.getDescription());

        Map<String,String> serviceProps = super.getServiceProps();
        serviceProps.putAll(props);
        
        addErrorReport(serviceProps);

        return serviceProps;
    }

    private Map<String, String> collectWorkerProps(AmazonMapReduceJobWorker worker) {
        Map<String, String> props = new HashMap<String, String>();

        // This overwrites existing value, but that should be fine.
        String error = worker.getError();
        if (error != null) {
            super.setError(error);
        }

        props.put(SYSTEM_PREFIX + "Worker Status",
                  worker.getJobStatus().name());


        if(worker instanceof AmazonMapReducePostJobWorker){
            AmazonMapReducePostJobWorker pjw = (AmazonMapReducePostJobWorker)worker;
           props.putAll(pjw.getBubbleableProperties());
           if(pjw.getError() != null){
               super.setError(pjw.getError());
           }
        }
        
        String jobId = worker.getJobId();
        if (jobId != null) {
            props.put(SYSTEM_PREFIX + "Job ID", jobId);
            Map<String, String> jobDetailsMap = worker.getJobDetailsMap();
            for (String key : jobDetailsMap.keySet()) {
                String value = jobDetailsMap.get(key);
                props.put(SYSTEM_PREFIX + key, value);

                if (value.contains("FAIL")) {
                    super.setError("Hadoop Job: " + value);
                }
            }
        }

        return props;
    }

    public void updated(Dictionary config) throws ConfigurationException {
        log("Attempt made to update " + getJobType() +
            " service configuration via updated method. " +
            "Updates should occur via class setters.");
    }

    public void createDestSpace() {
        if (null != getDestSpaceId()) {
            try {
                getContentStore().createSpace(getDestSpaceId());
            } catch (ContentStoreException e) {
                log.debug("Ensuring output space exists: " + e.getMessage());
            }
        }
    }

    public String getDuraStoreHost() {
        return duraStoreHost;
    }

    public void setDuraStoreHost(String duraStoreHost) {
        if (duraStoreHost != null && !duraStoreHost.equals("")) {
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
        if (duraStorePort != null) {
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
        if (duraStoreContext != null && !duraStoreContext.equals("")) {
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

    public String getSourceSpaceId() {
        return sourceSpaceId;
    }

    public void setSourceSpaceId(String sourceSpaceId) {
        if (sourceSpaceId != null && !sourceSpaceId.equals("")) {
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
        this.destSpaceId = destSpaceId;
    }

    public String getWorkSpaceId() {
        return workSpaceId;
    }

    public void setWorkSpaceId(String workSpaceId) {
        this.workSpaceId = workSpaceId;
    }

    public String getMappersPerInstance() {
        return mappersPerInstance;
    }

    public void setMappersPerInstance(String mappersPerInstance) {
        this.mappersPerInstance = mappersPerInstance;

        if (mappersPerInstance != null && !mappersPerInstance.equals("")) {
            try {
                Integer.valueOf(mappersPerInstance);
                this.mappersPerInstance = mappersPerInstance;
            } catch (NumberFormatException e) {
                log("Attempt made to set mappersPerInstance to a " +
                    "non-numerical value, which is not valid. Setting " +
                    "value to default: " + DEFAULT_NUM_MAPPERS);
                this.mappersPerInstance = DEFAULT_NUM_MAPPERS;
            }
        } else {
            log("Attempt made to set mappersPerInstance to to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_NUM_MAPPERS);
            this.mappersPerInstance = DEFAULT_NUM_MAPPERS;
        }
    }

    public ContentStore getContentStore() {
        if (null == contentStore) {
            ContentStoreManager storeManager = new ContentStoreManagerImpl(
                duraStoreHost,
                duraStorePort,
                duraStoreContext);
            storeManager.login(new Credential(username, password));

            try {
                contentStore = storeManager.getPrimaryContentStore();

            } catch (ContentStoreException e) {
                log.error(e.getMessage());
                throw new DuraCloudRuntimeException(e);
            }
        }
        return contentStore;
    }

    public String getOptimizeMode() {
        return optimizeMode;
    }

    public void setOptimizeMode(String optimizeMode) {
        this.optimizeMode = optimizeMode;
    }

    public String getNumInstances() {
        return numInstances;
    }

    public void setNumInstances(String numInstances) {
        if (numInstances != null && !numInstances.equals("")) {
            try {
                Integer.valueOf(numInstances);
                this.numInstances = numInstances;
            } catch (NumberFormatException e) {
                log.warn(
                    "Attempt made to set numInstances to a non-numerical " +
                        "value, which is not valid. Setting value to default: " +
                        DEFAULT_NUM_INSTANCES);
                this.numInstances = DEFAULT_NUM_INSTANCES;
            }
        } else {
            log.warn("Attempt made to set numInstances to to null or empty, " +
                         ", which is not valid. Setting value to default: " +
                         DEFAULT_NUM_INSTANCES);
            this.numInstances = DEFAULT_NUM_INSTANCES;
        }
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {

        if (instanceType != null && !instanceType.equals("")) {
            this.instanceType = instanceType;
        } else {
            log.warn("Attempt made to set typeOfInstance to null or empty, " +
                         ", which is not valid. Setting value to default: " +
                         DEFAULT_INSTANCE_TYPE);
            this.instanceType = DEFAULT_INSTANCE_TYPE;
        }
    }

    public String getOptimizeType() {
        return optimizeType;
    }

    public void setOptimizeType(String optimizeType) {
        this.optimizeType = optimizeType;
    }

    public String getCostNumInstances() {
        return costNumInstances;
    }

    public void setCostNumInstances(String costNumInstances) {
        this.costNumInstances = costNumInstances;
    }

    public String getCostInstanceType() {
        return costInstanceType;
    }

    public void setCostInstanceType(String costInstanceType) {
        this.costInstanceType = costInstanceType;
    }

    public String getSpeedNumInstances() {
        return speedNumInstances;
    }

    public void setSpeedNumInstances(String speedNumInstances) {
        this.speedNumInstances = speedNumInstances;
    }

    public String getSpeedInstanceType() {
        return speedInstanceType;
    }

    public void setSpeedInstanceType(String speedInstanceType) {
        this.speedInstanceType = speedInstanceType;
    }

    public void setContentStore(ContentStore contentStore) {
        this.contentStore = contentStore;
    }

    private void log(String logMsg) {
        log.warn(logMsg);
    }
}