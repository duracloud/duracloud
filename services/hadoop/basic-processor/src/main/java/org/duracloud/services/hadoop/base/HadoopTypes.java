/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

/**
 * This class contains constants for running hadoop jobs.
 *
 * @author Andrew Woods
 *         Date: Sep 28, 2010
 */
public class HadoopTypes {

    public enum TASK_PARAMS {
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

}
