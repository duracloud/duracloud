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

    public ServiceRegistryName(String version) {
        this.version = version;
    }

    public String getName() {
        String v = version.replaceAll("\\.", "-");
        return "duracloud-" + v.toLowerCase() + "-service-repo";
    }

}
