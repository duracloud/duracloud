/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.mgmt;

import org.duracloud.client.ServicesManager;

import java.io.InputStream;

/**
 * @author Andrew Woods
 *         Date: 6/19/11
 */
public interface LocalServicesManager extends ServicesManager {

    /**
     * Initializes the service manager with the provided XML document so
     * that the service manager is able to connect to all services,
     * service compute instances, and user storage.
     *
     * @param configXml the xml used to initialize the service manager
     */
    public void configure(InputStream configXml);
}
