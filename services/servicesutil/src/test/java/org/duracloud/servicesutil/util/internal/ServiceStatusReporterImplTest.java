/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.internal;

import org.duracloud.services.ComputeService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Dec 14, 2009
 */
public class ServiceStatusReporterImplTest {

    private ServiceStatusReporterImpl statusReporter;

    private String serviceIdA = "serviceA";
    private String serviceIdB = "serviceB";
    private String serviceIdC = "serviceC";
    private String serviceIdD = "serviceD";

    private List<String> serviceIds;

    @Before
    public void setUp() {
        serviceIds = new ArrayList<String>();
        serviceIds.add(serviceIdA);
        serviceIds.add(serviceIdB);
        serviceIds.add(serviceIdC);
        serviceIds.add(serviceIdD);
        List<ComputeService> duraServices = createServiceList(serviceIds);

        statusReporter = new ServiceStatusReporterImpl();
        statusReporter.setDuraServices(duraServices);
    }

    private List<ComputeService> createServiceList(List<String> serviceIds) {
        NoopServiceListerImpl forTest = new NoopServiceListerImpl();
        forTest.setDuraServices(serviceIds);

        return forTest.getDuraServices();
    }

    @After
    public void tearDown() {
        statusReporter = null;
        serviceIds = null;
    }

    @Test
    public void testGetStatus() throws Exception {
        ComputeService.ServiceStatus status = null;

        status = statusReporter.getStatus(serviceIdC);
        Assert.assertNotNull(status);
        Assert.assertEquals(ComputeService.ServiceStatus.INSTALLED, status);

        getService(serviceIdC).start();

        status = statusReporter.getStatus(serviceIdC);
        Assert.assertNotNull(status);
        Assert.assertEquals(ComputeService.ServiceStatus.STARTED, status);

        getService(serviceIdC).stop();

        status = statusReporter.getStatus(serviceIdC);
        Assert.assertNotNull(status);
        Assert.assertEquals(ComputeService.ServiceStatus.STOPPED, status);
    }

    @Test
    public void testMissingService() throws Exception {
        boolean thrown = false;
        try {
            statusReporter.getStatus("no-service");
            Assert.fail("Exception expected");
        } catch (Exception e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    private ComputeService getService(String serviceId) throws Exception {
        List<ComputeService> services = statusReporter.getDuraServices();
        for (ComputeService service : services) {
            if (serviceId.equalsIgnoreCase(service.getServiceId())) {
                return service;
            }
        }
        Assert.fail("Service not found: " + serviceId);
        return null;
    }

}
