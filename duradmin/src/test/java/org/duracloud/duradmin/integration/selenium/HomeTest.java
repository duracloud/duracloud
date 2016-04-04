/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.integration.selenium;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(getBaseURL() + "favicon.ico");
        HttpResponse response = client.execute(get);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertTrue(response.getHeaders("Content-Type").length  == 0);
    }

}
