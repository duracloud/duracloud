/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.bulkimageconversion;

import org.duracloud.services.ComputeService;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReduceService;
import org.duracloud.services.amazonmapreduce.postprocessing.MimePostJobWorker;
import org.duracloud.storage.domain.HadoopTypes;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * Service which converts image files from one format to another
 * in a bulk fashion by utilizing hadoop.
 *
 * @author Bill Branan
 *         Date: Aug 17, 2010
 */
public class BulkImageConversionService extends BaseAmazonMapReduceService implements ComputeService, ManagedService {

    private final Logger log = LoggerFactory.getLogger(
        BulkImageConversionService.class);

    private static final String DEFAULT_TO_FORMAT = "jp2";
    private static final String DEFAULT_COLORSPACE = "source";
    private static final String DEFAULT_NAME_PREFIX = "";
    private static final String DEFAULT_NAME_SUFFIX = "";

    private String toFormat;
    private String colorSpace;
    private String namePrefix;
    private String nameSuffix;
    private String optimizeMode;
    private String optimizeType;
    private String numInstances;
    private String instanceType;
    private String costNumInstances;
    private String costInstanceType;
    private String speedNumInstances;
    private String speedInstanceType;

    private AmazonMapReduceJobWorker worker;
    private AmazonMapReduceJobWorker postWorker;

    @Override
    protected AmazonMapReduceJobWorker getJobWorker() {
        if (null == worker) {
            worker = new HadoopJobWorker(getContentStore(),
                                         getWorkSpaceId(),
                                         collectTaskParams(),
                                         getServiceWorkDir());
        }
        return worker;
    }

    @Override
    protected AmazonMapReduceJobWorker getPostJobWorker() {
        if (null == postWorker) {
            postWorker = new MimePostJobWorker(getJobWorker(),
                                               getContentStore(),
                                               getDestSpaceId());
        }
        return postWorker;
    }

    @Override
    protected String getJobType() {
        return HadoopTypes.JOB_TYPES.BULK_IMAGE_CONVERSION.name();
    }

    @Override
    protected String getNumMappers(String instanceType) {
        String mappers = "1";
        if (HadoopTypes.INSTANCES.LARGE.getId().equals(instanceType)) {
            mappers = "2";

        } else if (HadoopTypes.INSTANCES.XLARGE.getId().equals(instanceType)) {
            mappers = "4";
        }
        return mappers;
    }

    @Override
    protected String getInstancesType() {
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

    @Override
    protected String getNumOfInstances() {
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
    protected Map<String, String> collectTaskParams() {
        Map<String, String> taskParams = super.collectTaskParams();

        taskParams.put(TASK_PARAMS.DEST_FORMAT.name(), toFormat);
        if (namePrefix != null) {
            taskParams.put(TASK_PARAMS.NAME_PREFIX.name(), namePrefix);
        }
        if (nameSuffix != null) {
            taskParams.put(TASK_PARAMS.NAME_SUFFIX.name(), nameSuffix);
        }
        if (colorSpace != null) {
            taskParams.put(TASK_PARAMS.COLOR_SPACE.name(), colorSpace);
        }

        return taskParams;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        worker = null;
        postWorker = null;
    }

    public String getToFormat() {
        return toFormat;
    }

    public void setToFormat(String toFormat) {
        if (toFormat != null && !toFormat.equals("")) {
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
        if (colorSpace != null && !colorSpace.equals("")) {
            this.colorSpace = colorSpace;
        } else {
            log("Attempt made to set colorSpace to null, which is not valid. " +
                "Setting value to default: " + DEFAULT_COLORSPACE);
            this.colorSpace = DEFAULT_COLORSPACE;
        }
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        log.debug("setting namePrefix: {}->{}", this.namePrefix, namePrefix);
        if (namePrefix != null) {
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
        log.debug("setting nameSuffix: {}->{}", this.nameSuffix, nameSuffix);
        if (nameSuffix != null) {
            this.nameSuffix = nameSuffix;
        } else {
            log("Attempt made to set nameSuffix to null, which is not valid. " +
                "Setting value to default: " + DEFAULT_NAME_SUFFIX);
            this.nameSuffix = DEFAULT_NAME_SUFFIX;
        }
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

    private void log(String logMsg) {
        log.warn(logMsg);
    }
}