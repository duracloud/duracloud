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
import org.duracloud.servicesutil.util.ServiceStarter;

import java.util.List;

public class ServiceStarterTester {

    private final ServiceStarter starter;
    private final ServiceLister lister;

    public ServiceStarterTester(ServiceStarter starter, ServiceLister lister) {
        this.starter = starter;
        this.lister = lister;
    }

    public void testServiceStarter() throws Exception {
        List<ComputeService> duraServices = lister.getDuraServices();
        assertNotNull(duraServices);
        assertFalse("There must be at least one service available to test",
                    duraServices.isEmpty());

        ComputeService service = duraServices.iterator().next();
        String testServiceId = service.getServiceId();
        service.stop();
        assertEquals(ComputeService.ServiceStatus.STOPPED,
                     service.getServiceStatus());

        starter.start(testServiceId);

        duraServices = lister.getDuraServices();
        assertNotNull(duraServices);
        service = null;
        for(ComputeService computeService : duraServices) {
            if(computeService.getServiceId().equals(testServiceId)) {
                service = computeService;
            }
        }
        assertNotNull(service);
        assertEquals(ComputeService.ServiceStatus.STARTED,
                     service.getServiceStatus());
    }

}