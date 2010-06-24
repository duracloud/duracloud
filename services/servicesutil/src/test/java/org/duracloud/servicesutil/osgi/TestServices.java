/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.osgi;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.duracloud.services.ComputeService;
import org.duracloud.services.common.util.BundleHome;
import org.duracloud.servicesutil.util.DuraConfigAdmin;
import org.duracloud.servicesutil.util.ServiceInstaller;
import org.duracloud.servicesutil.util.ServiceLister;
import org.duracloud.servicesutil.util.ServicePropsFinder;
import org.duracloud.servicesutil.util.ServiceStarter;
import org.duracloud.servicesutil.util.ServiceStatusReporter;
import org.duracloud.servicesutil.util.ServiceStopper;
import org.duracloud.servicesutil.util.ServiceUninstaller;
import org.duracloud.servicesutil.util.catalog.BundleCatalog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestServices extends AbstractDuracloudOSGiTestBasePax {

    private final Logger log = LoggerFactory.getLogger(TestServices.class);

    private static final String HELLOSERVICE_FILTER = "(duraService=helloservice)";

    private final int MAX_TRIES = 10;

    private ServiceInstaller installer;

    private ServiceUninstaller uninstaller;

    private ServiceLister lister;

    private ServiceStarter starter;

    private ServiceStopper stopper;

    private ServiceStatusReporter statusReporter;

    private ServicePropsFinder propsFinder;

    private DuraConfigAdmin configAdmin;

    private ComputeService helloService;

    @Before
    public void setUp() throws Exception {
        BundleHome bundleHome = getInstaller().getBundleHome();
        FileUtils.cleanDirectory(bundleHome.getAttic());
        FileUtils.cleanDirectory(bundleHome.getWork());
        FileUtils.cleanDirectory(bundleHome.getContainer());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    @After
    public void tearDown() {
        BundleCatalog.clearCatalog();
    }

    @Test
    public void testServiceInstaller() throws Exception {
        log.debug("testing ServiceInstaller");

        ServiceInstallerTester tester = new ServiceInstallerTester(getInstaller());
        tester.testServiceInstaller();
    }

    @Test
    public void testServiceUninstaller() throws Exception {
        log.debug("testing ServiceUninstaller");

        ServiceUninstallerTester tester = new ServiceUninstallerTester(
            getUninstaller());

        tester.testServiceUninstaller();
    }

    @Test
    public void testServiceInstallationCycle() throws Exception {
        log.debug("testing ServiceInstallationCycle");

        ServiceInstallationCycleTester tester = new ServiceInstallationCycleTester(
            getInstaller(),
            getUninstaller());

        tester.testServiceInstallationCycle();
    }

    @Test
    public void testServiceLister() throws Exception {
        log.debug("testing ServiceLister");

        ServiceListerTester tester = new ServiceListerTester(getLister());
        tester.testServiceLister();
    }

    @Test
    public void testServiceStarter() throws Exception {
        log.debug("testing ServiceStarter");

        ServiceStarterTester tester = new ServiceStarterTester(getStarter(),
                                                               getLister());
        tester.testServiceStarter();
    }

    @Test
    public void testServiceStopper() throws Exception {
        log.debug("testing ServiceStopper");

        ServiceStopperTester tester = new ServiceStopperTester(getStopper(),
                                                               getLister());
        tester.testServiceStopper();
    }

    @Test
    public void testServiceStatusReporter() throws Exception {
        log.debug("testing ServiceStatusReporter");

        ServiceStatusReporterTester tester = new ServiceStatusReporterTester(
            getStatusReporter(),
            getLister());
        tester.testServiceStatusReporter();
    }

    @Test
    public void testServicePropsFinder() throws Exception {
        log.debug("testing ServicePropsFinder");

        ServicePropsFinderTester tester = new ServicePropsFinderTester(
            getPropsFinder(),
            getLister());
        tester.testServicePropsFinder();
    }

    @Test
    public void testConfigAdmin() throws Exception {
        log.debug("testing ConfigurationAdmin");

        ConfigAdminTester tester = new ConfigAdminTester(getConfigAdmin(),
                                                         getHelloService());
        tester.testConfigAdmin();
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

    public ServiceInstaller getInstaller() throws Exception {
        installer = (ServiceInstaller) getService(ServiceInstaller.class.getName());
        Assert.assertNotNull(installer);
        return installer;
    }

    public ServiceUninstaller getUninstaller() throws Exception {
        uninstaller = (ServiceUninstaller) getService(ServiceUninstaller.class.getName());
        Assert.assertNotNull(uninstaller);
        return uninstaller;
    }

    public ServiceLister getLister() throws Exception {
        lister = (ServiceLister) getService(ServiceLister.class.getName());
        Assert.assertNotNull(lister);
        return lister;
    }

    public ServiceStarter getStarter() throws Exception {
        starter = (ServiceStarter) getService(ServiceStarter.class.getName());
        Assert.assertNotNull(starter);
        return starter;
    }

    public ServiceStopper getStopper() throws Exception {
        stopper = (ServiceStopper) getService(ServiceStopper.class.getName());
        Assert.assertNotNull(stopper);
        return stopper;
    }

    private ServiceStatusReporter getStatusReporter() throws Exception {
        statusReporter = (ServiceStatusReporter) getService(
            ServiceStatusReporter.class.getName());
        Assert.assertNotNull(statusReporter);
        return statusReporter;
    }

    private ServicePropsFinder getPropsFinder() throws Exception {
        propsFinder = (ServicePropsFinder) getService(ServicePropsFinder.class.getName());
        Assert.assertNotNull(propsFinder);
        return propsFinder;
    }

    public DuraConfigAdmin getConfigAdmin() throws Exception {
        configAdmin = (DuraConfigAdmin) getService(DuraConfigAdmin.class.getName());
        Assert.assertNotNull(configAdmin);
        return configAdmin;
    }

    public ComputeService getHelloService() throws Exception {
        helloService = (ComputeService) getService(ComputeService.class.getName(),
                                                   HELLOSERVICE_FILTER);
        return helloService;
    }

}
