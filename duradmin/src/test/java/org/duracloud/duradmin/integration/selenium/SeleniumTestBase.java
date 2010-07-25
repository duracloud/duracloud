/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.integration.selenium;

import com.thoughtworks.selenium.SeleneseTestCase;

public abstract class SeleniumTestBase
        extends SeleneseTestCase {
    protected static final long DEFAULT_SELENIUM_PAGE_WAIT_IN_MS = 20000;
    public void setUp() throws Exception {
        setUp("http://localhost:8080/duradmin/", "*firefox");
    }

    protected String getBaseURL(){
     return "http://localhost:8080/duradmin/";   
    }
    
    protected void goHome() throws Exception {
        selenium.open(getBaseURL());
    }

    protected void clickAndWait(String pattern) {
        selenium.click(pattern);
        selenium.waitForPageToLoad(String.valueOf(DEFAULT_SELENIUM_PAGE_WAIT_IN_MS));
        
    }
}
