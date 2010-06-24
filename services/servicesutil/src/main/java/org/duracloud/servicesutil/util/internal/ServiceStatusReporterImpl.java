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
import org.duracloud.servicesutil.util.ServiceStatusReporter;
import org.duracloud.servicesutil.util.internal.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Dec 14, 2009
 */
public class ServiceStatusReporterImpl implements ServiceStatusReporter {

    private final Logger log = LoggerFactory.getLogger(ServiceStatusReporterImpl.class);

    private List<ComputeService> duraServices;
    private ServiceHelper helper = new ServiceHelper();


    public ComputeService.ServiceStatus getStatus(String serviceId) {
        log.debug("Status for: '" + serviceId + "'");
        ComputeService service = helper.findService(serviceId, duraServices);
        return doGetStatus(service);
    }

    private ComputeService.ServiceStatus doGetStatus(ComputeService service) {
        try {
            return service.getServiceStatus();
        } catch (Exception e) {
            String msg = "Error with service status: " + service.getServiceId();
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