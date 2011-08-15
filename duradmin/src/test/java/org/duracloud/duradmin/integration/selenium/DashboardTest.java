/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.integration.selenium;


/**
 * 
 * @author Daniel Bernstein
 *
 */
public class DashboardTest
        extends SeleniumTestBase{

    private static final String COMPLETED_RADIO_SELECTOR = "css=#completed-radio";

    public void setUp() throws Exception {
        super.setUp();
        navigateToDashboardPage();
    }
   
    public void testCompletedServices() throws Exception {
        selenium.click("css=#services-tab-link");
        assertTrue(selenium.isVisible(COMPLETED_RADIO_SELECTOR));
        selenium.click(COMPLETED_RADIO_SELECTOR);
        assertTrue(selenium.isVisible("css=#completed-services-panel"));
        //the first element is used as a hidden template.
        String locator = "css=#service-viewer .service:nth-child(2)";
        isPresentAndVisible(locator, DEFAULT_SELENIUM_PAGE_WAIT_IN_MS);
    }

}
