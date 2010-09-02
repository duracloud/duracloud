/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.internal.util;

import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Sep 2, 2010
 */
public class ServiceHelperTest {

    private ServiceHelper serviceHelper = new ServiceHelper();

    private List<String> serviceIds;
    private List<ComputeService> services;

    @Before
    public void setUp() {
        String prefix = "prefix";
        String base = "base";
        String version1 = "0.1.1";
        String version2 = "0.1.2";
        
        serviceIds = new ArrayList<String>();
        serviceIds.add(base);
        serviceIds.add(base + "-" + version1);
        serviceIds.add(base + "-" + version2);
        serviceIds.add(prefix + "-" + base);
        serviceIds.add(prefix + "-" + base + "-" + version1);
        serviceIds.add(prefix + "-" + base + "-" + version2);

        services = new ArrayList<ComputeService>();
        for (int i = 0; i < serviceIds.size(); ++i) {
            services.add(new LocalComputeService(serviceIds.get(i)));
        }
    }

    @Test
    public void testFindService() throws Exception {
        ComputeService service = null;
        for (String serviceId : serviceIds) {
            service = serviceHelper.findService(serviceId + ".zip", services);
            Assert.assertNotNull(service);

            Assert.assertEquals(serviceId, service.getServiceId());
        }
    }

    private class LocalComputeService extends BaseService {
        public LocalComputeService(String id) {
            super.setServiceId(id);
        }
    }

}
