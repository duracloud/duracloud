/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.webapputil.osgi;

import junit.framework.Assert;
import org.duracloud.services.webapputil.WebAppUtil;
import org.duracloud.services.ComputeService;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Andrew Woods
 *         Date: Dec 7, 2009
 */
public class TestServices extends AbstractDuracloudOSGiTestBasePax {

    private final Logger log = LoggerFactory.getLogger(TestServices.class);

    private final int MAX_TRIES = 10;
    private WebAppUtil webappUtil;

    @Before
    public void setUp() throws Exception {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        String workDir = "target/webapputil-test";
        File serviceWorkDir = new File(workDir);
        serviceWorkDir.mkdirs();
        getWebappUtil().setServiceWorkDir(workDir);
    }

    @Test
    public void testWebAppUtil() throws Exception {
        log.debug("testing WebAppUtilImpl");

        WebAppUtilTester tester = new WebAppUtilTester(getWebappUtil());
        tester.testWebAppUtil();

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
        Assert.assertNotNull("service not found: " + serviceInterface, refs[0]);
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

    public WebAppUtil getWebappUtil() throws Exception {
        if (webappUtil == null) {
            webappUtil = (WebAppUtil) getService(ComputeService.class.getName(),
                                                 "(duraService=webapputilservice)");
        }
        Assert.assertNotNull(webappUtil);
        return webappUtil;
    }

}