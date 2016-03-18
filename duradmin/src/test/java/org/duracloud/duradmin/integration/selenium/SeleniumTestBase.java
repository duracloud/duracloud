/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.integration.selenium;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.selenium.SeleneseTestCase;

public abstract class SeleniumTestBase
        extends SeleneseTestCase {
    protected static final long DEFAULT_SELENIUM_PAGE_WAIT_IN_MS = 20000;
    protected static Logger log = LoggerFactory.getLogger(SeleniumTestBase.class);
    public void setUp() throws Exception {
        String defaultUrl = "http://localhost:8080/duradmin/";
        setUp(defaultUrl, "*firefox");
        selenium.open(defaultUrl);
        login();
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

    protected void logout() {
        clickAndWait("css=.logoff");
        Assert.assertTrue(selenium.isTextPresent("Login"));

    }

    protected void navigateToSpacesPage() throws Exception{
        clickAndWait("css=#spaces-tab a");
        assertTrue(selenium.isElementPresent("css=#provider-logo"));
    }

    
    protected void login() throws Exception{
        log.debug("source"+selenium.getHtmlSource());
        selenium.type("css=#username", "root");
        selenium.type("css=#password", "rpw");
        clickAndWait("css=#button-login");
        // Thread.sleep(1000);
        Assert.assertTrue(selenium.isElementPresent("css=.logoff"));
    }

    protected void isPresentAndVisible(String locator, long waitForMs)
        throws InterruptedException {
            boolean elementPresent = false;
            long time = System.currentTimeMillis();
        
            while(true){
                if(selenium.isElementPresent(locator) &&selenium.isVisible(locator)){
                    elementPresent = true;
                    break;
                }else{
                    if(System.currentTimeMillis()-time > waitForMs){
                        break;
                    }else{
                        Thread.sleep(100);
                    }
                    
                }
            }
            
            assertTrue(elementPresent);
        }
}
