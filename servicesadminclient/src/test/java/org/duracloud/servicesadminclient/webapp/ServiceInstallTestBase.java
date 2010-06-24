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
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.servicesadminclient.ServicesAdminClient;
import org.junit.Assert;

import java.io.File;

/**
 * @author Andrew Woods
 *         Date: Dec 14, 2009
 */
public class ServiceInstallTestBase {

    private File testBundle;
    private ServicesAdminClient client;

    public ServiceInstallTestBase(ServicesAdminClient client, File testBundle) {
        Assert.assertNotNull(client);
        Assert.assertNotNull(testBundle);
        Assert.assertTrue(testBundle.exists());

        this.client = client;
        this.testBundle = testBundle;
    }

    protected void installTestBundle() throws Exception {
        RestHttpHelper.HttpResponse response = getClient().postServiceBundle(
            getTestBundle());
        junit.framework.Assert.assertNotNull(response);

        int statusCode = response.getStatusCode();
        junit.framework.Assert.assertEquals(HttpURLConnection.HTTP_OK,
                                            statusCode);
    }

    protected void uninstallTestBundle() throws Exception {
        RestHttpHelper.HttpResponse response = getClient().deleteServiceBundle(
            getTestBundle().getName());
        assertNotNull(response);

        int statusCode = response.getStatusCode();
        assertEquals(HttpURLConnection.HTTP_OK, statusCode);
    }

    protected File getTestBundle() {
        return testBundle;
    }

    protected void setTestBundle(File testBundle) {
        this.testBundle = testBundle;
    }

    protected ServicesAdminClient getClient() {
        return client;
    }

    protected void setClient(ServicesAdminClient client) {
        this.client = client;
    }
}
