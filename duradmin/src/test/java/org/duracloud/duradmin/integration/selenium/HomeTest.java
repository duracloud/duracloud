/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.integration.selenium;


public class HomeTest
        extends SeleniumTestBase{

    public void setUp() throws Exception {
        setUp("http://localhost:8080/duradmin/", "*firefox");
    }

    public void testHome() throws Exception {
        goHome();
        assertTrue(selenium.isTextPresent("Welcome"));
        assertTrue(selenium.isElementPresent("storageProviderId"));
    }
}
