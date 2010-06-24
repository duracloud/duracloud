/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.internal;

import org.duracloud.services.ComputeService;
import org.duracloud.services.common.error.ServiceRuntimeException;
import org.duracloud.servicesutil.util.ServiceStopper;
import org.duracloud.servicesutil.util.internal.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ServiceStopperImpl implements ServiceStopper {

    private final Logger log = LoggerFactory.getLogger(ServiceStopperImpl.class);

    private List<ComputeService> duraServices;
    private ServiceHelper helper = new ServiceHelper();

    /**
     * {@inheritDoc}
     */
    public void stop(String serviceId) {
        log.info("Stopping Service: " + serviceId);
        ComputeService service = helper.findService(serviceId, duraServices);
        doStop(service);
    }

    private void doStop(ComputeService service) {
        try {
            service.stop();
        } catch (Exception e) {
            String msg = "Error stopping service: " + service.getServiceId();
            log.error(msg);
            throw new ServiceRuntimeException(msg, e);
        }
    }

    public List<ComputeService> getDuraServices() {
        return duraServices;
    }

    public void setDuraServices(List<ComputeService> duraServices) {
        this.duraServices = duraServices;
    }

}