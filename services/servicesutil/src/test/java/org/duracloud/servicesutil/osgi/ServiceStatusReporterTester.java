/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.osgi;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import org.duracloud.services.ComputeService;
import org.duracloud.servicesutil.util.ServiceLister;
import org.duracloud.servicesutil.util.ServiceStatusReporter;
import org.junit.Assert;

import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Dec 14, 2009
 */
public class ServiceStatusReporterTester {

    private final ServiceStatusReporter statusReporter;
    private final ServiceLister lister;

    public ServiceStatusReporterTester(ServiceStatusReporter statusReporter,
                                       ServiceLister lister) {
        Assert.assertNotNull(statusReporter);
        Assert.assertNotNull(lister);

        this.statusReporter = statusReporter;
        this.lister = lister;
    }

    public void testServiceStatusReporter() throws Exception {

        List<ComputeService> duraServices = lister.getDuraServices();
        assertNotNull(duraServices);
        assertFalse("There must be at least one service available to test",
                    duraServices.isEmpty());

        for (ComputeService service : duraServices) {
            String serviceId = service.getServiceId();
            Assert.assertNotNull(serviceId,
                                 statusReporter.getStatus(serviceId));
        }

        try {
            statusReporter.getStatus("no-service");
            Assert.fail("Exception expected.");
        } catch (Exception e) {
            // do nothing.
        }

    }
}
