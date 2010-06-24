/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.osgi;

import java.util.List;

import org.duracloud.services.ComputeService;
import org.duracloud.servicesutil.util.ServiceLister;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class ServiceListerTester {

    private final ServiceLister lister;

    private final static String TEST_COMPUTE_SERVICE = "HelloService";

    public ServiceListerTester(ServiceLister lister) {
        this.lister = lister;
    }

    public void testServiceLister() throws Exception {
        List<ComputeService> duraServices = lister.getDuraServices();
        assertNotNull(duraServices);

        verifyComputeServicesFound(duraServices);

    }

    private void verifyComputeServicesFound(List<ComputeService> computeServices)
            throws Exception {
        boolean testComputeServiceFound = false;
        for (ComputeService duraService : computeServices) {
            String serviceDesc = duraService.describe();

            if (!testComputeServiceFound) {
                testComputeServiceFound =
                        serviceFound(serviceDesc, TEST_COMPUTE_SERVICE);
            }
        }
        assertTrue(testComputeServiceFound);
    }

    private boolean serviceFound(String serviceName, String targetName) {
        return serviceName.contains(targetName);
    }
}
