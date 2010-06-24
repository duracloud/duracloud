/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util;

import org.duracloud.services.common.util.BundleHome;

public interface ServiceUninstaller {

    public abstract void uninstall(String serviceId) throws Exception;

    public abstract BundleHome getBundleHome();

}