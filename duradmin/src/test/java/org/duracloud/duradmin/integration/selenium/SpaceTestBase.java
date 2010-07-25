/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.integration.selenium;

import java.util.Date;



public class SpaceTestBase
        extends SeleniumTestBase{

    
    
    public void setUp() throws Exception {
        super.setUp();
        
    }
   
    protected void navigateToSpacesPage() throws Exception{
        goHome();
        clickAndWait("//a[@id='spacesMenuItem']");
        assertTrue(selenium.isTextPresent("Metadata"));
    }

    protected void removeSpace(String spaceId) throws Exception {
        navigateToSpacesPage();
        selenium.mouseOver("//tr[@id='"+ spaceId + "']");
        selenium.click("//tr[@id='"+ spaceId + "']//a[@id='removeSpaceLink']");
        assertTrue(selenium.isConfirmationPresent());
        selenium.getConfirmation();
        selenium.waitForPageToLoad(String.valueOf(DEFAULT_SELENIUM_PAGE_WAIT_IN_MS));
        navigateToSpacesPage();
        assertFalse(selenium.isElementPresent("//tr[@id='" + spaceId + "']"));
    }

    protected String addSpace() throws Exception {
        String spaceId = "test-space-selenium-" + new Date().getTime();
        clickAndWait("//a[@id='addSpaceLink']");
        assertTrue(selenium.isElementPresent("//label[@id='spaceIdLabel']"));
        selenium.type("//input[@id='spaceId']", spaceId);
        clickAndWait("//input[@id='addSpaceButton']");
        navigateToSpacesPage();
        assertTrue(selenium.isElementPresent("//tr[@id='" + spaceId + "']"));
        return spaceId;
    }

}
