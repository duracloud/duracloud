/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.model.Credential;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.security.domain.SecurityUserBean;
import org.duracloud.security.xml.SecurityUsersDocumentBinding;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Andrew Woods
 *         Date: Apr 19, 2010
 */
public class TestSecurityRest extends BaseRestTester {

    private final static String spacePrefix = "test-store-security-";
    private final static String contentPrefix = "test-content-";
    private static String spaceId;
    private static String contentId;

    @BeforeClass
    public static void beforeClass() throws Exception {
        BaseRestTester.beforeClass();
        createSpace();
        setSpaceAccess("CLOSED");
    }

    private static void createSpace() throws Exception {
        HttpCaller caller = new HttpCaller() {
            protected HttpResponse call() throws Exception {
                return RestTestHelper.addSpace(getSpaceId());
            }
        };
        caller.makeCall(201);
    }

    private static void setSpaceAccess(String access) throws Exception {
        final String url = getSpaceUrl();
        final String content = null;
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("x-dura-meta-space-access", access);

        HttpCaller caller = new HttpCaller() {
            protected HttpResponse call() throws Exception {
                return restHelper.post(url, content, headers);
            }
        };
        caller.makeCall(200);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        RestTestHelper.deleteSpace(getSpaceId());
    }

    @Test
    public void testValidContentAccess() throws Exception {
        List<String> grants = new ArrayList<String>();
        grants.add("ROLE_USER");
        doContentAccessTest(grants, 200);
    }

    @Test
    public void testInvalidContentAccess() throws Exception {
        List<String> grants = new ArrayList<String>();
        doContentAccessTest(grants, 403);
    }

    private void doContentAccessTest(List<String> grants, int status)
        throws Exception {
        Credential cred = new Credential("test-user", "pw");
        setUserSecurity(cred, grants);

        final String url = getSpaceUrl();
        final RestHttpHelper userRestHelper = new RestHttpHelper(cred);

        // check access to user
        HttpCaller headCaller = new HttpCaller() {
            protected HttpResponse call() throws Exception {
                return userRestHelper.head(url);
            }
        };
        headCaller.makeCall(status);
    }

    private void setUserSecurity(Credential cred, List<String> grants)
        throws Exception {
        SecurityUserBean userBean = new SecurityUserBean(cred.getUsername(),
                                                         cred.getPassword(),
                                                         grants);
        List<SecurityUserBean> users = new ArrayList<SecurityUserBean>();
        users.add(userBean);

        final String securityUrl = RestTestHelper.getBaseUrl() + "/security";
        final String xml = SecurityUsersDocumentBinding.createDocumentFrom(users);
        final Map<String, String> headers = null;

        // add security user
        HttpCaller caller = new HttpCaller() {
            protected HttpResponse call() throws Exception {
                return restHelper.post(securityUrl, xml, headers);
            }
        };
        caller.makeCall(200);
    }

    @Test
    public void testValidContentWrite() throws Exception {
        List<String> grants = new ArrayList<String>();
        grants.add("ROLE_USER");
        doContentWriteTest(grants, 201);
    }

    @Test
    public void testInvalidContentWrite() throws Exception {
        List<String> grants = new ArrayList<String>();
        doContentWriteTest(grants, 403);
    }

    private void doContentWriteTest(List<String> grants, int status)
        throws Exception {
        Credential cred = new Credential("test-user", "pw");
        setUserSecurity(cred, grants);

        final Map<String, String> headers = null;
        final String url = getContentUrl();
        final RestHttpHelper userRestHelper = new RestHttpHelper(cred);

        // check access to user
        HttpCaller headCaller = new HttpCaller() {
            protected HttpResponse call() throws Exception {
                return userRestHelper.put(url, "hello", headers);
            }
        };
        headCaller.makeCall(status);
    }

    private static String getSpaceId() {
        if (null == spaceId) {
            Random r = new Random();
            spaceId = spacePrefix + r.nextInt(10000);
        }
        return spaceId;
    }

    private static String getContentId() {
        if (null == contentId) {
            Random r = new Random();
            contentId = contentPrefix + r.nextInt(10000);
        }
        return contentId;
    }

    private static String getSpaceUrl() throws Exception {
        return RestTestHelper.getBaseUrl() + "/" + getSpaceId();
    }

    private static String getContentUrl() throws Exception {
        return getSpaceUrl() + "/" + getContentId();
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
