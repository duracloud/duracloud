/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.durastore.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.duracloud.common.constant.Constants;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.SimpleCredential;
import org.duracloud.common.test.TestConfig;
import org.duracloud.common.test.TestConfigUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;

/**
 * @author Bill Branan
 */
public class RestTestHelper {

    private static RestHttpHelper restHelper = getAuthorizedRestHelper();

    private static String baseUrl;

    public static final String PROPERTIES_NAME =
        Constants.HEADER_PREFIX + "test-properties";

    public static final String PROPERTIES_VALUE = "Test Properties";

    public static HttpResponse addSpace(String spaceID)
            throws Exception {
        String url = getBaseUrl() + "/" + spaceID;
        return addSpaceWithHeaders(url);
    }

    public static HttpResponse addSpace(String spaceID, String storeID)
            throws Exception {
        String url = getBaseUrl() + "/" + spaceID + "?storeID=" + storeID;
        return addSpaceWithHeaders(url);
    }

    private static HttpResponse addSpaceWithHeaders(String url)
            throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(PROPERTIES_NAME, PROPERTIES_VALUE);
        return restHelper.put(url, null, headers);
    }

    public static HttpResponse deleteSpace(String spaceID)
            throws Exception {
        String url = getBaseUrl() + "/" + spaceID;
        return restHelper.delete(url);
    }

    public static HttpResponse deleteSpace(String spaceID, String storeID)
            throws Exception {
        String url = getBaseUrl() + "/" + spaceID + "?storeID=" + storeID;
        return restHelper.delete(url);
    }

    public static String getBaseUrl() throws Exception {
        if (baseUrl == null) {
            TestConfig config = new TestConfigUtil().getTestConfig();
            baseUrl =
                "http" + ((config.getTestEndPoint().getPort()+"").equals("443") ? "s" : "")
                      + "://"
                      + config.getTestEndPoint().getHost()
                      + ":"
                      + config.getTestEndPoint().getPort()
                      + "/"
                      + "durastore";
        }
        return baseUrl;
    }

    public static RestHttpHelper getAuthorizedRestHelper() {
        try {
            SimpleCredential cred = new TestConfigUtil().getTestConfig().getRootCredential();
            return new RestHttpHelper(new Credential(cred.getUsername(), cred.getPassword()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
