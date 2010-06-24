/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadminclient.webapp;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.services.beans.ComputeServiceBean;
import org.duracloud.services.util.ServiceSerializer;
import org.duracloud.services.util.XMLServiceSerializerImpl;
import org.duracloud.servicesadminclient.ServicesAdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class ServiceInstallUninstallFlowTester extends ServiceInstallTestBase {

    private final Logger log = LoggerFactory.getLogger(ServiceInstallUninstallFlowTester.class);
    
    private ServiceSerializer serializer;

    public ServiceInstallUninstallFlowTester(File testBundle,
                                             ServicesAdminClient client) {
        super(client, testBundle);
    }

    public void testNewServiceFlow() throws Exception {
        // Allow tomcat to come up.
        Thread.sleep(5000);

        // check new service does not exist
        verifyTestServiceIsListed(false);

        // install service
        installTestBundle();

        // Allow test-service to come up.
        Thread.sleep(5000);

        // check new service exists and available in container
        verifyTestServiceIsListed(true);

        // uninstall service
        uninstallTestBundle();

        // Allow test-service to go down.
        Thread.sleep(5000);

        // check new service does not exist
        verifyTestServiceIsListed(false);
    }

    private void verifyTestServiceIsListed(boolean exists) throws Exception {
        HttpResponse response = getClient().getServiceListing();
        assertNotNull(response);

        int statusCode = response.getStatusCode();
        assertEquals(HttpURLConnection.HTTP_OK, statusCode);

        String body = response.getResponseBody();
        assertNotNull(body);

        List<ComputeServiceBean> beans = getSerializer().deserializeList(body);
        boolean testServiceFound = false;
        for (ComputeServiceBean bean : beans) {
            String serviceDesc = bean.getServiceName();
            log.debug("dura-service: " + serviceDesc);

            if (!testServiceFound) {
                testServiceFound =
                        TestServiceAdminWepApp.testServiceFound(serviceDesc);
            }
        }
        assertEquals(exists, testServiceFound);

    }

    private ServiceSerializer getSerializer() {
        if (serializer == null) {
            serializer = new XMLServiceSerializerImpl();
        }
        return serializer;
    }
}
