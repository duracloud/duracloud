/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.duradmin;

import java.io.IOException;

import org.duracloud.common.model.Credential;
import org.duracloud.common.model.SimpleCredential;
import org.duracloud.common.test.TestConfigUtil;
import org.duracloud.common.test.TestEndPoint;
import org.duracloud.common.web.RestHttpHelper;

/**
 * @author Andrew Woods
 *         Date: Apr 19, 2010
 */
public class RestTestHelper {

    public static RestHttpHelper getAuthorizedRestHelper() {
        try {
            SimpleCredential creds = new TestConfigUtil().getTestConfig().getRootCredential();
            return new RestHttpHelper(new Credential(creds.getUsername(), creds.getPassword()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String getBaseUrl(){
        try {
            TestEndPoint endpoint = new TestConfigUtil().getTestConfig().getTestEndPoint();
            return "http" + (endpoint.getPort().equals("443") ? "s" : "") + "://" + endpoint.getHost() + ":" + endpoint.getPort() + "/durastore";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
