/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 6/22/11
 */
public class ServiceSummaryTest {

    private ServiceSummary summary;

    private ServiceInfo serviceInfo;
    private Map<String, String> serviceProperties;

    @Before
    public void setUp() throws Exception {
        summary = new ServiceSummary();
        serviceInfo = new ServiceInfo();
        serviceProperties = new HashMap<String, String>();
    }

    @After
    public void tearDown() throws Exception {
        serviceInfo = null;
        serviceProperties = null;
    }

    @Test
    public void test() throws Exception {
        summary.setServiceInfo(serviceInfo);
        summary.setServiceProperties(serviceProperties);

        Assert.assertEquals(serviceInfo, summary.getServiceInfo());
        Assert.assertEquals(serviceProperties, summary.getServiceProperties());
    }
}
