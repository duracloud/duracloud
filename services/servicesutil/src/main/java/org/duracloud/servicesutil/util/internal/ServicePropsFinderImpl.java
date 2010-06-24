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
import org.duracloud.servicesutil.util.internal.util.ServiceHelper;
import org.duracloud.servicesutil.util.ServicePropsFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Dec 18, 2009
 */
public class ServicePropsFinderImpl implements ServicePropsFinder {

    private final Logger log = LoggerFactory.getLogger(ServicePropsFinderImpl.class);

    private List<ComputeService> duraServices;
    private ServiceHelper helper = new ServiceHelper();


    public Map<String, String> getProps(String serviceId) {
        log.debug("Finding props for: '" + serviceId + "'");
        ComputeService service = helper.findService(serviceId, duraServices);
        return doGetProps(service);
    }

    private Map<String, String> doGetProps(ComputeService service) {
        try {
            return service.getServiceProps();
        } catch (Exception e) {
            String msg = "Error with service props : " + service.getServiceId();
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
