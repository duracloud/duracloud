/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.common.util;

import org.duracloud.services.common.error.ServiceRuntimeException;

import java.io.File;

/**
 * @author Andrew Woods
 *         Date: Dec 13, 2009
 */
public class BundleHome {

    private static final String BUNDLE_HOME_PROP = "BUNDLE_HOME";
    protected String baseDir;

    /**
     * Internal management directories.
     */
    private final String CONTAINER = "container";
    private final String ATTIC = "attic";
    private final String WORK = "work";

    public BundleHome() {        
        String home = System.getProperty(BUNDLE_HOME_PROP);
        if (null == home || home.length() == 0) {
            String s = "System property: ${" + BUNDLE_HOME_PROP + "} not found";
            System.err.println(s);
            throw new ServiceRuntimeException(s);
        }
        this.baseDir = home;
    }

    public File getFromContainer(String name) {
        return new File(getContainer(), name);
    }

    public File getFromAttic(String name) {
        return new File(getAttic(), name);
    }

    public File getServiceWork(String serviceId) {
        File serviceWork = new File(getWork(), serviceId);
        if (!serviceWork.exists()) {
            serviceWork.mkdir();
        }
        return serviceWork;
    }

    public File getContainer() {
        File container = new File(getBaseDir(), CONTAINER);
        if (!container.exists()) {
            container.mkdir();
        }
        return container;
    }

    public File getAttic() {
        File attic = new File(getBaseDir(), ATTIC);
        if (!attic.exists()) {
            attic.mkdir();
        }
        return attic;
    }

    public File getWork() {
        File work = new File(getBaseDir(), WORK);
        if (!work.exists()) {
            work.mkdir();
        }
        return work;
    }

    public File getHome() {
        return new File(getBaseDir());
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
}
