/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.internal;

import org.duracloud.servicesutil.util.ServiceStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoopServiceStarterImpl
        implements ServiceStarter {

    private final Logger log = LoggerFactory.getLogger(NoopServiceStarterImpl.class);

    /**
     * {@inheritDoc}
     */
    public void start(String serviceId) {
        log.info("Service started: " + serviceId + " ...not really");
    }

}
