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
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReduceService;
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
            postWorker = new PostJobWorker(getJobWorker(),
                                           getContentStore(),
                                           getToFormat(),
                                           getDestSpaceId());
        }
        return postWorker;
    }

    @Override
    protected String getJobType() {
        return "bulk-image-conversion";
    }

    @Override
    protected Map<String, String> collectTaskParams() {
        Map<String, String> taskParams = super.collectTaskParams();

        taskParams.put(TASK_PARAMS.DEST_FORMAT.name(), toFormat);
        if (namePrefix != null && !namePrefix.equals("")) {
            taskParams.put(TASK_PARAMS.NAME_PREFIX.name(), namePrefix);
        }
        if (nameSuffix != null && !nameSuffix.equals("")) {
            taskParams.put(TASK_PARAMS.NAME_SUFFIX.name(), nameSuffix);
        }
        if (colorSpace != null) {
            taskParams.put(TASK_PARAMS.COLOR_SPACE.name(), colorSpace);
        }

        return taskParams;
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
        if (nameSuffix != null) {
            this.nameSuffix = nameSuffix;
        } else {
            log("Attempt made to set nameSuffix to null, which is not valid. " +
                "Setting value to default: " + DEFAULT_NAME_SUFFIX);
            this.nameSuffix = DEFAULT_NAME_SUFFIX;
        }
    }

    private void log(String logMsg) {
        log.warn(logMsg);
    }
}