/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.integration.selenium;



public class ServicesTestBase
        extends SeleniumTestBase{

    
    
    public void setUp() throws Exception {
        super.setUp();
        
    }
   
    protected void navigateToServicesPage() throws Exception{
        goHome();
        clickAndWait("//a[@id='servicesMenuItem']");
        assertTrue(selenium.isTextPresent("Deployed"));
    }

}
