/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.util;

import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.duracloud.services.beans.ComputeServiceBean;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class XMLServiceSerializerImplTest {

    private XMLServiceSerializerImpl serializer;

    private final String serviceName0 = "org.test.service0";

    private final String serviceName1 = "org.test.service1";

    private final String serviceName2 = "org.test.service2";

    @Test
    public void testSerializeDeserializeList() throws Exception {
        serializer = new XMLServiceSerializerImpl();

        List<ComputeService> services = new ArrayList<ComputeService>();
        services.add(new MockComputeService(serviceName0));
        services.add(new MockComputeService(serviceName1));
        services.add(new MockComputeService(serviceName2));

        String serialized = serializer.serialize(services);
        Assert.assertNotNull(serialized);

        Assert.assertTrue(serialized.contains(serviceName0));
        Assert.assertTrue(serialized.contains(serviceName1));
        Assert.assertTrue(serialized.contains(serviceName2));

        List<ComputeServiceBean> beans = serializer.deserializeList(serialized);
        Assert.assertNotNull(beans);

        boolean foundService0 = false;
        boolean foundService1 = false;
        boolean foundService2 = false;
        for (ComputeServiceBean bean : beans) {
            if (serviceName0.equals(bean.getServiceName())) {
                foundService0 = true;
            } else if (serviceName1.equals(bean.getServiceName())) {
                foundService1 = true;
            } else if (serviceName2.equals(bean.getServiceName())) {
                foundService2 = true;
            }
        }

        Assert.assertTrue(foundService0);
        Assert.assertTrue(foundService1);
        Assert.assertTrue(foundService2);
    }

    @Test
    public void testSerializeDeserializeBean() throws Exception {
        serializer = new XMLServiceSerializerImpl();

        ComputeServiceBean service = new ComputeServiceBean(serviceName0);

        String xml = serializer.serialize(service);
        Assert.assertNotNull(xml);
        Assert.assertTrue(xml.contains(serviceName0));

        ComputeServiceBean bean = serializer.deserializeBean(xml);
        Assert.assertNotNull(bean);
        Assert.assertEquals(serviceName0, bean.getServiceName());

    }

    private class MockComputeService extends BaseService
            implements ComputeService {

        private final String name;

        public MockComputeService(String name) {
            this.name = name;
        }

        @Override
        public String describe() throws Exception {
            return name;
        }

        @Override
        public String getServiceId() {
            return name;
        }
    }

}
