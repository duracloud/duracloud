/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util;

import org.duracloud.services.common.error.ServiceException;
import org.duracloud.services.common.util.BundleHome;

import java.io.InputStream;

public interface ServiceInstaller {

    public abstract void install(String name, InputStream bundle)
            throws ServiceException;

    public abstract BundleHome getBundleHome();

}