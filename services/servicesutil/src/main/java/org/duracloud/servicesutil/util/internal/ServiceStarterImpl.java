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
import org.duracloud.services.common.util.BundleHome;
import org.duracloud.servicesutil.util.ServiceStarter;
import org.duracloud.servicesutil.util.internal.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class ServiceStarterImpl implements ServiceStarter {

    private final Logger log = LoggerFactory.getLogger(ServiceStarterImpl.class);

    private List<ComputeService> duraServices;
    private ServiceHelper helper = new ServiceHelper();
    private BundleHome bundleHome;

    /**
     * {@inheritDoc}
     */
    public void start(String serviceId) {
        log.info("Starting Service: " + serviceId);
        ComputeService service = helper.findService(serviceId, duraServices);
        doSetWorkDir(service);
        doStart(service);
    }

    private void doSetWorkDir(ComputeService service) {
        File serviceWorkDir =
            getBundleHome().getServiceWork(service.getServiceId());
        if(!serviceWorkDir.exists()) {
            serviceWorkDir.mkdir();
        }
        service.setServiceWorkDir(serviceWorkDir.getAbsolutePath());
    }

    private void doStart(ComputeService service) {
        try {
            service.start();
        } catch (Exception e) {
            String msg = "Error starting service: " + service.getServiceId();
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

    public BundleHome getBundleHome() {
        return bundleHome;
    }

    public void setBundleHome(BundleHome bundleHome) throws Exception {
        this.bundleHome = bundleHome;
    }

}