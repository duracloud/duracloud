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

    private int id = 5;
    private int deploymentId = 7;
    private String name = "name";
    private String version = "1.2.3";
    private Map<String, String> properties = new HashMap<String, String>();
    private Map<String, String> configs = new HashMap<String, String>();

    @Before
    public void setUp() throws Exception {
        summary = new ServiceSummary();
    }

    @After
    public void tearDown() throws Exception {
        summary = null;
    }

    @Test
    public void test() throws Exception {
        summary.setId(id);
        summary.setDeploymentId(deploymentId);
        summary.setName(name);
        summary.setVersion(version);
        summary.setConfigs(configs);
        summary.setProperties(properties);

        Assert.assertEquals(id, summary.getId());
        Assert.assertEquals(deploymentId, summary.getDeploymentId());
        Assert.assertEquals(name, summary.getName());
        Assert.assertEquals(version, summary.getVersion());
        Assert.assertEquals(configs, summary.getConfigs());
        Assert.assertEquals(properties, summary.getProperties());
    }
}
