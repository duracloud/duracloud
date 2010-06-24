/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.DuraCloudUserType;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.Assert;

/**
 * @author Andrew Woods
 *         Date: Apr 20, 2010
 */
public class ClientTestBase {

    private static String host = "localhost";
    private static String port = null;
    private static String defaultPort = "8080";
    private static String context = "durastore";

    protected static String getBaseUrl() throws Exception {
        return "http://" + host + ":" + getPort() + "/" + context;
    }

    protected static String getHost() {
        return host;
    }

    protected static String getContext() {
        return context;
    }

    protected static String getPort() throws Exception {
        if (port == null) {
            StoreClientConfig config = new StoreClientConfig();
            port = config.getPort();
        }

        try { // Ensure the port is a valid port value
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            port = defaultPort;
        }

        return port;
    }

    protected static RestHttpHelper getAuthorizedRestHelper() {
        return new RestHttpHelper(getRootCredential());
    }

    private static Credential getRootCredential() {
        UnitTestDatabaseUtil dbUtil = null;
        try {
            dbUtil = new UnitTestDatabaseUtil();
        } catch (Exception e) {
            System.err.println("ERROR from unitTestDB: " + e.getMessage());
        }

        Credential rootCredential = null;
        try {
            ResourceType rootUser = ResourceType.fromDuraCloudUserType(
                DuraCloudUserType.ROOT);
            rootCredential = dbUtil.findCredentialForResource(rootUser);
        } catch (Exception e) {
            System.err.print("ERROR getting credential: " + e.getMessage());

        }
        return rootCredential;
    }

    /**
     * This class spins on the abstract 'call()' until it returns the expected
     * status code or it has run out of tries.
     */
    public static abstract class HttpCaller {
        public void makeCall(int expected) throws Exception {
            RestHttpHelper.HttpResponse response = call();
            Assert.assertNotNull(response);
            int tries = 0;
            final int maxTries = 10;
            while (response.getStatusCode() != expected && tries < maxTries) {
                response = call();
                Assert.assertNotNull(response);
                tries++;
            }

            Assert.assertEquals(expected, response.getStatusCode());
        }

        protected abstract RestHttpHelper.HttpResponse call() throws Exception;
    }
}
