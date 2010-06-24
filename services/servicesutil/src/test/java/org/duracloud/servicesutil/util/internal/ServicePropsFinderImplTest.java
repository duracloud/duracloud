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
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Dec 18, 2009
 */
public class ServicePropsFinderImplTest {

    private ServicePropsFinderImpl propsFinder;

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

        propsFinder = new ServicePropsFinderImpl();
        propsFinder.setDuraServices(duraServices);
    }

    private List<ComputeService> createServiceList(List<String> serviceIds) {
        NoopServiceListerImpl forTest = new NoopServiceListerImpl();
        forTest.setDuraServices(serviceIds);

        return forTest.getDuraServices();
    }

    @After
    public void tearDown() {
        propsFinder = null;
        serviceIds = null;
    }

    @Test
    public void testGetProps() throws Exception {
        verifyProps(serviceIdA, true);
        verifyProps(serviceIdB, true);
        verifyProps(serviceIdC, true);
        verifyProps(serviceIdD, true);
        verifyProps("bad-id", false);
    }

    private void verifyProps(String serviceId, boolean expected) {
        Map<String, String> props = null;
        boolean exceptionThrown = false;
        try {
            props = propsFinder.getProps(serviceId);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        Assert.assertEquals(expected, !exceptionThrown);

        if (expected) {
            Assert.assertNotNull(props);
            Assert.assertTrue(props.size() > 0);

            Assert.assertEquals(expected, props.containsKey("serviceId"));
            String value = props.get("serviceId");
            Assert.assertNotNull(value);
            Assert.assertEquals(serviceId, value);
        }

    }

}
