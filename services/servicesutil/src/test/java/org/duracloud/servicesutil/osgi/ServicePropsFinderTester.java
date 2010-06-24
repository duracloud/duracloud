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
import org.duracloud.servicesutil.util.ServicePropsFinder;
import org.junit.Assert;

import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Dec 18, 2009
 */
public class ServicePropsFinderTester {

    private ServicePropsFinder propsFinder;
    private ServiceLister lister;

    public ServicePropsFinderTester(ServicePropsFinder propsFinder,
                                    ServiceLister lister) {
        Assert.assertNotNull(propsFinder);
        Assert.assertNotNull(lister);

        this.propsFinder = propsFinder;
        this.lister = lister;
    }

    public void testServicePropsFinder() {
        List<ComputeService> duraServices = lister.getDuraServices();
        assertNotNull(duraServices);
        assertFalse("There must be at least one service available to test",
                    duraServices.isEmpty());

        for (ComputeService service : duraServices) {
            String serviceId = service.getServiceId();
            Map<String, String> props = propsFinder.getProps(serviceId);
            Assert.assertNotNull(serviceId, props);

            String value = props.get("serviceId");
            Assert.assertNotNull(serviceId, value);
            Assert.assertEquals(serviceId, value);
        }

        try {
            propsFinder.getProps("no-service");
            Assert.fail("Exception expected.");
        } catch (Exception e) {
            // do nothing.
        }

    }
}
