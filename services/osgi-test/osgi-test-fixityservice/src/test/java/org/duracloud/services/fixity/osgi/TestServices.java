/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.osgi;

import junit.framework.Assert;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.services.ComputeService;
import org.duracloud.services.common.util.BundleHome;
import org.duracloud.services.fixity.FixityService;
import org.duracloud.servicesutil.util.DuraConfigAdmin;
import org.duracloud.unittestdb.util.StorageAccountTestUtil;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Dictionary;
import java.util.Enumeration;

/**
 * @author Andrew Woods
 *         Date: Aug 16, 2010
 */
public class TestServices extends AbstractDuracloudOSGiTestBasePax {

    private final Logger log = LoggerFactory.getLogger(TestServices.class);

    private final int MAX_TRIES = 10;

    @BeforeClass
    public static void initializeDurastore() throws Exception {
        StorageAccountTestUtil acctUtil = new StorageAccountTestUtil();
        RestHttpHelper.HttpResponse response = acctUtil.initializeDurastore(
            "localhost",
            getPort(),
            "durastore");
        Assert.assertNotNull(response);
    }

    private static String getPort() {
        String port = System.getProperty(STORE_PORT_PROP);
        Assert.assertNotNull(port);
        return port;
    }

    @After
    public void tearDown() throws BundleException {
        // FIXME: tearDown is only here for debugging.
        Bundle[] bundles = bundleContext.getBundles();
        Assert.assertNotNull(bundles);
        log.info("num bundles: " + bundles.length);
        for (Bundle bundle : bundles) {
            String name = bundle.getSymbolicName();
            int state = bundle.getState();
            log.info("bundle: '" + name + "' [" + state + "]");
            Dictionary headers = bundle.getHeaders();
            if (state == 2) {
                log.info("headers:");
                Enumeration headerItr = headers.elements();
                while (headerItr.hasMoreElements()) {
                    String header = (String) headerItr.nextElement();
                    log.info("[" + header + "]");

                }
            }
        }
    }

    @Test
    public void testFixity() throws Exception {
        String port = getPort();

        String version = System.getProperty(PROJECT_VERSION_PROP);
        Assert.assertNotNull(version);

        FixityService fixity = getFixityService();

        BundleHome bundleHome = getBundleHome();
        File workDir = bundleHome.getServiceWork(fixity.getServiceId());

        log.debug("testing Fixity Service, port: " + port);
        FixityTester tester = new FixityTester(getFixityService(),
                                               getConfigAdmin(),
                                               workDir,
                                               port,
                                               version);
        tester.testFixity();
    }

    private BundleHome getBundleHome() {
        String home = System.getProperty(BUNDLE_HOME_PROP);
        Assert.assertNotNull(home);

        return new BundleHome();
    }

    protected Object getService(String serviceInterface) throws Exception {
        return getService(serviceInterface, null);
    }

    private Object getService(String serviceInterface, String filter)
        throws Exception {
        ServiceReference[] refs = bundleContext.getServiceReferences(
            serviceInterface,
            filter);

        int count = 0;
        while ((refs == null || refs.length == 0) && count < MAX_TRIES) {
            count++;
            log.debug("Trying to find service: '" + serviceInterface + "'");
            Thread.sleep(1000);
            refs = bundleContext.getServiceReferences(serviceInterface, filter);
        }
        Assert.assertNotNull("service not found: " + serviceInterface, refs[0]);
        log.debug(getPropsText(refs[0]));
        return bundleContext.getService(refs[0]);
    }

    private String getPropsText(ServiceReference ref) {
        StringBuilder sb = new StringBuilder("properties:");
        for (String key : ref.getPropertyKeys()) {
            sb.append("\tprop: [" + key);
            sb.append(":" + ref.getProperty(key) + "]\n");
        }
        return sb.toString();
    }

    public DuraConfigAdmin getConfigAdmin() throws Exception {
        DuraConfigAdmin configAdmin = (DuraConfigAdmin) getService(
            DuraConfigAdmin.class.getName());
        Assert.assertNotNull(configAdmin);
        return configAdmin;
    }

    public FixityService getFixityService() throws Exception {
        FixityService fixityService = (FixityService) getService(ComputeService.class.getName(),
                                                                 "(duraService=fixityservice)");
        Assert.assertNotNull(fixityService);
        return fixityService;
    }

}
