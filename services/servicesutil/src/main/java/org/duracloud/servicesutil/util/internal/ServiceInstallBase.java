/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.internal;

import org.apache.commons.io.FilenameUtils;
import org.duracloud.services.common.error.ServiceException;
import org.duracloud.services.common.util.BundleHome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrew Woods
 *         Date: Oct 1, 2009
 */
public abstract class ServiceInstallBase {
    protected final Logger log = LoggerFactory.getLogger(ServiceInstallBase.class);

    private BundleHome bundleHome;

    abstract protected void init() throws Exception;

    protected boolean isJar(String name) throws ServiceException {
        return FilenameUtils.getExtension(name).equalsIgnoreCase("jar");
    }

    protected boolean isWar(String name) throws ServiceException {
        return FilenameUtils.getExtension(name).equalsIgnoreCase("war");
    }

    protected boolean isZip(String name) throws ServiceException {
        return FilenameUtils.getExtension(name).equalsIgnoreCase("zip");
    }

    protected void throwServiceException(String msg) throws ServiceException {
        log.error("Error: " + msg);
        throw new ServiceException(msg);
    }

    public BundleHome getBundleHome() {
        return bundleHome;
    }

    public void setBundleHome(BundleHome bundleHome) throws Exception {
        this.bundleHome = bundleHome;
        init();
    }

}
