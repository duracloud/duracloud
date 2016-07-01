/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.duracloud.common.model.Credential;
import org.duracloud.common.model.SimpleCredential;
import org.duracloud.common.test.TestConfig;
import org.duracloud.common.test.TestConfigUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.junit.Assert;

/**
 * @author Andrew Woods
 *         Date: Apr 20, 2010
 */
public class ClientTestBase {

    private static TestConfig testConfig = getTestConfig();
    
    protected static RestHttpHelper restHelper = getAuthorizedRestHelper();

    private static String context = "durastore";
    
    protected static TestConfig getTestConfig(){
        try {
           return  new TestConfigUtil().getTestConfig();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    protected static String getBaseUrl() throws Exception {
        return "http" + (getPort().equals("443") ? "s" : "") + "://" + getHost() + ":" + getPort() + "/" + context;
    }

    protected static String getContext() {
        return context;
    }
    
    protected static String getHost(){
        return testConfig.getTestEndPoint().getHost();
    }

    protected static String getPort(){
        return testConfig.getTestEndPoint().getPort()+"";
    }

    protected static RestHttpHelper getAuthorizedRestHelper() {
        return new RestHttpHelper(getRootCredential());
    }

    protected static Credential getRootCredential() {
        SimpleCredential cred =  testConfig.getRootCredential();
        return new Credential(cred.getUsername(), cred.getPassword());
    }


    protected static void createSpace(final String url) throws Exception {
        ClientTestBase.HttpCaller caller = new ClientTestBase.HttpCaller() {
            protected RestHttpHelper.HttpResponse call() throws Exception {
                String content = null;
                Map<String, String> headers = new HashMap<String, String>();
                return restHelper.put(url, content, headers);
            }
        };
        caller.makeCall(201);
    }


    protected static void createContent(final String url) throws Exception {
        HttpCaller caller = new HttpCaller() {
            protected RestHttpHelper.HttpResponse call() throws Exception {
                Map<String, String> headers = null;
                return restHelper.put(url, "hello", headers);
            }
        };
        caller.makeCall(201);
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
