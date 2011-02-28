/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

/**
 * This class contains constants for running hadoop jobs.
 *
 * @author Andrew Woods
 *         Date: Sep 28, 2010
 */
public class HadoopTypes {
    public static final String RUN_HADOOP_TASK_NAME = "run-hadoop-job";
    public static final String STOP_JOB_TASK_NAME = "stop-hadoop-job";
    public static final String DESCRIBE_JOB_TASK_NAME = "describe-hadoop-job";

    public enum TASK_PARAMS {
        JOB_TYPE("-"),
        WORKSPACE_ID("-"),
        BOOTSTRAP_CONTENT_ID("-"),
        JAR_CONTENT_ID("-"),
        SOURCE_SPACE_ID("sourceSpaceId"),
        DEST_SPACE_ID("destSpaceId"),
        INSTANCE_TYPE("-"),
        NUM_INSTANCES("-"),
        MAPPERS_PER_INSTANCE("-"),
        // image conversion params
        OUTPUT_SPACE_ID("outputSpaceId"),
        DEST_FORMAT("destFormat"),
        COLOR_SPACE("colorSpace"),
        NAME_PREFIX("namePrefix"),
        NAME_SUFFIX("nameSuffix"),
        // replication on demand params
        REP_STORE_ID("repStoreId"),
        REP_SPACE_ID("repSpaceId"),
        DC_HOST("dcHost"),
        DC_PORT("dcPort"),
        DC_CONTEXT("dcContext"),
        DC_USERNAME("dcUsername"),
        DC_PASSWORD("dcPassword"),
        DC_STORE_ID("dcStoreId"), // TODO: not currently used.
        INPUT_PATH("inputPath"),
        OUTPUT_PATH("outputPath");

        private String jarParam;

        TASK_PARAMS(String jarParam) {
            this.jarParam = jarParam;
        }

        public String getLongForm() {
            return jarParam;
        }

        public String getCliForm() {
            return "-" + getLongForm();
        }
    }

    public enum JOB_TYPES {
        BULK_IMAGE_CONVERSION,
        AMAZON_FIXITY,
        AMAZON_FIXITY_METADATA,
        REP_ON_DEMAND;
    }

    public enum TASK_OUTPUTS {
        JOB_FLOW_ID,
        RESULTS;
    }

    public enum INSTANCES {
        SMALL("Small Instance", "m1.small"),
        LARGE("Large Instance", "m1.large"),
        XLARGE("Extra Large Instance", "m1.xlarge");

        private String desc;
        private String id;

        INSTANCES(String desc, String id) {
            this.desc = desc;
            this.id = id;
        }

        public String getDescription() {
            return desc;
        }

        public String getId() {
            return id;
        }
    }

}
