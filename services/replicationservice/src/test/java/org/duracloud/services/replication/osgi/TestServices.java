/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.replication.osgi;

import junit.framework.Assert;
import org.duracloud.services.ComputeService;
import org.duracloud.services.replication.ReplicationService;
import org.duracloud.servicesutil.util.DuraConfigAdmin;
import org.junit.After;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestServices
        extends AbstractDuracloudOSGiTestBasePax {

    private final Logger log = LoggerFactory.getLogger(TestServices.class);

    private final int MAX_TRIES = 10;

    @After
    public void tearDown() throws BundleException {
        // FIXME: tearDown is only here for debugging. Can be removed when
        //        ReplicationTester is fixed.
        Bundle[] bundles = bundleContext.getBundles();
        Assert.assertNotNull(bundles);
        log.info("num bundles: " + bundles.length);
        for (Bundle bundle : bundles) {
            String name = bundle.getSymbolicName();
            int state = bundle.getState();
            log.info("bundle: '" + name + "' [" + state + "]");
            if (name.contains("activemq") || name.contains("jms")) {
//                bundle.uninstall();
            }
        }
    }

    @Test
    public void testDynamicConfig() throws Exception {
        log.debug("testing Dynamic Configuration of Replication Service");

        DynamicConfigTester tester =
                new DynamicConfigTester(getConfigAdmin(),
                                        getReplicationService());
        tester.testDynamicConfig();

    }

    @Test
    public void testReplication() throws Exception {
        log.debug("testing Content Replication of Replication Service");

        ReplicationTester tester = new ReplicationTester(getReplicationService());
        tester.testReplication();
    }

    protected Object getService(String serviceInterface) throws Exception {
        return getService(serviceInterface, null);
    }

    private Object getService(String serviceInterface, String filter)
            throws Exception {
        ServiceReference[] refs =
                bundleContext.getServiceReferences(serviceInterface, filter);

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
        DuraConfigAdmin configAdmin =
                (DuraConfigAdmin) getService(DuraConfigAdmin.class.getName());
        Assert.assertNotNull(configAdmin);
        return configAdmin;
    }

    public ReplicationService getReplicationService() throws Exception {
        ReplicationService replicationService =
                (ReplicationService) getService(ComputeService.class.getName(),
                                                "(duraService=replication)");
        Assert.assertNotNull(replicationService);
        return replicationService;
    }

}
