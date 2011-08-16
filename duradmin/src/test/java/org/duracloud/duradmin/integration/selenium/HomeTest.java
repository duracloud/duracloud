/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.integration.selenium;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeTest extends SeleniumTestBase {
    protected static Logger log = LoggerFactory.getLogger(HomeTest.class);

    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testHome() throws Exception {
        goHome();
        assertTrue(selenium.isElementPresent("css=#dc-tabs-panel"));
    }

    public void testForFavicon() throws Exception {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(getBaseURL() + "favicon.ico");
        get.setFollowRedirects(false);
        assertEquals(200, client.executeMethod(get));
        assertTrue(get.getResponseHeaders("Content-Type").length == 0);
    }

}
