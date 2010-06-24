/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.osgi;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import org.duracloud.services.ComputeService;
import org.duracloud.servicesutil.util.ServiceLister;
import org.duracloud.servicesutil.util.ServiceStopper;

import java.util.List;

public class ServiceStopperTester {

    private final ServiceStopper stopper;
    private final ServiceLister lister;

    public ServiceStopperTester(ServiceStopper stopper, ServiceLister lister) {
        this.stopper = stopper;
        this.lister = lister;
    }

    public void testServiceStopper() throws Exception {
        List<ComputeService> duraServices = lister.getDuraServices();
        assertNotNull(duraServices);
        assertFalse("There must be at least one service available to test",
                    duraServices.isEmpty());

        ComputeService service = duraServices.iterator().next();
        String testServiceId = service.getServiceId();
        service.start();
        assertEquals(ComputeService.ServiceStatus.STARTED,
                     service.getServiceStatus());

        stopper.stop(testServiceId);

        duraServices = lister.getDuraServices();
        assertNotNull(duraServices);
        service = null;
        for(ComputeService computeService : duraServices) {
            if(computeService.getServiceId().equals(testServiceId)) {
                service = computeService;
            }
        }
        assertNotNull(service);
        assertEquals(ComputeService.ServiceStatus.STOPPED,
                     service.getServiceStatus());
    }

}