/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.xml;

import org.duracloud.ServiceDocument;
import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Nov 24, 2009
 */
public class ServiceDocumentBindingTest {

    private ServiceInfo inputService;

    @Before
    public void setUp() {
        inputService = new ServiceInfo();
        List<DeploymentOption> deploymentOptions = new ArrayList<DeploymentOption>();
        deploymentOptions.add(new DeploymentOption());
        inputService.setDeploymentOptions(deploymentOptions);
    }

    @After
    public void tearDown() {
        inputService = null;
    }

    @Test
    public void testMaxDeploymentsAllowed() {
        int max = -2;
        int maxExpected = -1;
        inputService.setMaxDeploymentsAllowed(max);
        verifyMax(inputService, max, maxExpected);

        max = -1;
        maxExpected = -1;
        inputService.setMaxDeploymentsAllowed(max);
        verifyMax(inputService, max, maxExpected);

        max = 0;
        maxExpected = 0;
        inputService.setMaxDeploymentsAllowed(max);
        verifyMax(inputService, max, maxExpected);

        max = 1;
        maxExpected = 1;
        inputService.setMaxDeploymentsAllowed(max);
        verifyMax(inputService, max, maxExpected);
    }

    private void verifyMax(ServiceInfo inputService, int max, int maxExpected) {
        Assert.assertEquals(max, inputService.getMaxDeploymentsAllowed());

        ServiceDocument doc = ServiceDocumentBinding.createDocumentFrom(
            inputService);
        Assert.assertNotNull(doc);

        ServiceInfo service = ServiceDocumentBinding.createServiceFrom(doc.newInputStream());
        Assert.assertNotNull(service);
        Assert.assertEquals(maxExpected, service.getMaxDeploymentsAllowed());
    }
}
