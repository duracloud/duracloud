/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.model.Credential;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.security.domain.SecurityUserBean;
import org.duracloud.security.xml.SecurityUsersDocumentBinding;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Apr 19, 2010
 */
public class TestSecurityRest {

    private static RestHttpHelper restHelper = RestTestHelper.getAuthorizedRestHelper();
    private String servicesUrl;

    @Before
    public void setUp() throws Exception {
        servicesUrl = RestTestHelper.getBaseServiceUrl() + "/services";

        // Initialize DuraService
        RestHttpHelper.HttpResponse response = RestTestHelper.initialize();
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @Test
    public void testValidServiceAccess() throws Exception {
        List<String> grants = new ArrayList<String>();
        grants.add("ROLE_USER");
        doServiceAccessTest(grants, 200);
    }

    @Test
    public void testInvalidContentAccess() throws Exception {
        List<String> grants = new ArrayList<String>();
        doServiceAccessTest(grants, 403);
    }

    private void doServiceAccessTest(List<String> grants, int status)
        throws Exception {
        Credential cred = new Credential("test-user", "pw");
        setUserSecurity(cred, grants);

        final RestHttpHelper userRestHelper = new RestHttpHelper(cred);

        // check access to user
        HttpCaller caller = new HttpCaller() {
            protected RestHttpHelper.HttpResponse call() throws Exception {
                return userRestHelper.get(servicesUrl);
            }
        };
        caller.makeCall(status);
    }

    private void setUserSecurity(Credential cred, List<String> grants)
        throws Exception {
        SecurityUserBean userBean = new SecurityUserBean(cred.getUsername(),
                                                         cred.getPassword(),
                                                         grants);
        List<SecurityUserBean> users = new ArrayList<SecurityUserBean>();
        users.add(userBean);

        // set service security
        final String baseServiceUrl = RestTestHelper.getBaseServiceUrl();
        doSetUserSecurity(baseServiceUrl, users);

        // set store security
        final String baseStoreUrl = RestTestHelper.getBaseStoreUrl();
        doSetUserSecurity(baseStoreUrl, users);
    }

    private void doSetUserSecurity(String baseUrl, List<SecurityUserBean> users)
        throws Exception {
        final String url = baseUrl + "/security";
        final String xml = SecurityUsersDocumentBinding.createDocumentFrom(users);
        final Map<String, String> headers = null;

        // add security user
        HttpCaller caller = new HttpCaller() {
            protected RestHttpHelper.HttpResponse call() throws Exception {
                return restHelper.post(url, xml, headers);
            }
        };
        caller.makeCall(200);
    }

    /**
     * This class spins on the abstract 'call()' until it returns the expected
     * status code or it has run out of tries.
     */
    private static abstract class HttpCaller {
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
