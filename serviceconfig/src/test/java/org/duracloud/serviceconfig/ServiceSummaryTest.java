/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import org.duracloud.services.ComputeService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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

    @Test
    public void testCompare() {
        String startTime1 = "2011-01-01T10:00:00";
        String startTime2 = "2011-02-02T10:00:00";
        String stopTime1 = "2011-03-03T11:00:00";
        String stopTime2 = "2011-04-04T11:00:00";

        Map<String, String> properties2 = new HashMap<String, String>();

        // Both summaries empty
        ServiceSummary summary2 = null;
        assertEquals(0, summary.compareTo(summary2));

        // summary with start time, summary2 empty
        properties.put(ComputeService.STARTTIME_KEY, startTime1);
        summary.setProperties(properties);
        assertEquals(-1, summary.compareTo(summary2));

        // summary empty, summary2 with start time
        summary.setProperties(null);
        summary2 = new ServiceSummary();
        summary2.setProperties(properties);
        assertEquals(1, summary.compareTo(summary2));

        // both with start time
        summary.setProperties(properties);
        properties2.put(ComputeService.STARTTIME_KEY, startTime2);
        summary2.setProperties(properties2);
        assertEquals(-1, summary.compareTo(summary2));

        // summary with stop time
        properties.put(ComputeService.STOPTIME_KEY, stopTime1);
        assertEquals(1, summary.compareTo(summary2));

        // summary2 with stop time
        properties2.put(ComputeService.STOPTIME_KEY, stopTime2);
        assertEquals(-1, summary.compareTo(summary2));
    }

}
