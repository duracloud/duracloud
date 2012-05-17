/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.model;

/**
 * @author Andrew Woods
 *         Date: 4/13/11
 */
public class ServiceRegistryName {

    private String version;

    private static final String PREFIX = "duracloud-";
    private static final String SUFFIX = "-service-repo";
    private static final String PROFESSIONAL = "-pro";
    private static final String TRIAL = "-trial";

    public ServiceRegistryName(String version) {
        this.version = version;
    }

    public String getName() {
        String v = version.replaceAll("\\.", "-");
        return PREFIX + v.toLowerCase() + SUFFIX;
    }

    public String getNameProfessional() {
        String v = version.replaceAll("\\.", "-");
        return PREFIX + v.toLowerCase() + SUFFIX + PROFESSIONAL;
    }

    public String getNameTrial() {
        String v = version.replaceAll("\\.", "-");
        return PREFIX + v.toLowerCase() + SUFFIX + TRIAL;
    }

}
