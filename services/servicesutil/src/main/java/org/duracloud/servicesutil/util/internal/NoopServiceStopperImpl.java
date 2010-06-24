/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.internal;

import org.duracloud.servicesutil.util.ServiceStopper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoopServiceStopperImpl
        implements ServiceStopper {

    private final Logger log = LoggerFactory.getLogger(NoopServiceStopperImpl.class);

    /**
     * {@inheritDoc}
     */
    public void stop(String serviceId) {
        log.info("Service stopped: " + serviceId + " ...not really");
    }

}
