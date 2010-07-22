/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.streaming.osgi;

import static junit.framework.Assert.assertNotNull;
import org.apache.commons.io.FileUtils;
import org.duracloud.services.ComputeService;
import org.duracloud.services.streaming.MediaStreamingService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Tests the Media Streaming Service.
 *
 * To run this test use: mvn clean install -P profile-systest
 *
 * @author Bill Branan
 *         Date: May 13, 2010
 */
public class TestServices extends AbstractDuracloudOSGiTestBasePax {

    private final Logger log = LoggerFactory.getLogger(TestServices.class);

    private final int MAX_TRIES = 10;

    private MediaStreamingService service;

    @Before
    public void setUp() throws Exception {
        String workDir = "target/media-streaming-test";
        File serviceWorkDir = new File(workDir);
        serviceWorkDir.mkdirs();

        getService().setServiceWorkDir(workDir);
    }

    @After
    public void tearDown() throws Exception {
        File workDir = new File(getService().getServiceWorkDir());
        FileUtils.deleteDirectory(workDir);
    }

    @Test
    public void testMediaStreamingService() throws Exception {
        log.debug("testing MediaStreamingService");

        MediaStreamingServiceTester tester =
            new MediaStreamingServiceTester(getService());
        tester.testMediaStreamingService();
    }

    protected Object getService(String serviceInterface) throws Exception {
        return getService(serviceInterface, null);
    }

    private Object getService(String serviceInterface, String filter)
        throws Exception {
        ServiceReference[] refs = getBundleContext().getServiceReferences(
            serviceInterface,
            filter);

        int count = 0;
        while ((refs == null || refs.length == 0) && count < MAX_TRIES) {
            count++;
            log.debug("Trying to find service: '" + serviceInterface + "'");
            Thread.sleep(1000);
            refs = getBundleContext().getServiceReferences(serviceInterface,
                                                           filter);
        }
        assertNotNull("service not found: " + serviceInterface, refs[0]);
        log.debug(getPropsText(refs[0]));
        return getBundleContext().getService(refs[0]);
    }

    private String getPropsText(ServiceReference ref) {
        StringBuilder sb = new StringBuilder("properties:");
        for (String key : ref.getPropertyKeys()) {
            sb.append("\tprop: [" + key);
            sb.append(":" + ref.getProperty(key) + "]\n");
        }
        return sb.toString();
    }

    public MediaStreamingService getService() throws Exception {
        if (service == null) {
            service =
                (MediaStreamingService) getService(ComputeService.class.getName(),
                                                    "(duraService=mediastreaming)");
        }
        assertNotNull(service);
        return service;
    }

}