/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadminclient.webapp;

import org.apache.commons.io.FilenameUtils;
import org.duracloud.services.ComputeService;
import org.duracloud.servicesadminclient.ServicesAdminClient;
import org.junit.Assert;

import java.io.File;

/**
 * @author Andrew Woods
 *         Date: Dec 14, 2009
 */
public class ServiceStatusReporterTester extends ServiceInstallTestBase {

    public ServiceStatusReporterTester(File testBundle,
                                       ServicesAdminClient client) {
        super(client, testBundle);
    }

    public void testGetStatus() throws Exception {
          // Allow tomcat to come up.
        Thread.sleep(5000);

        // install service
        installTestBundle();

        // Allow test-service to come up.
        Thread.sleep(5000);

        String serviceId = FilenameUtils.getBaseName(getTestBundle().getName());

        ComputeService.ServiceStatus status = getClient().getServiceStatus(serviceId);
        Assert.assertNotNull(status);
        Assert.assertEquals(ComputeService.ServiceStatus.INSTALLED, status);

        uninstallTestBundle();
    }

}
