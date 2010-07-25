/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.integration.selenium;



public class ServicesHomeTest
        extends ServicesTestBase{

    
    
    public void setUp() throws Exception {
        super.setUp();
        
    }
   
    public void testDeployedServices() throws Exception {
        navigateToServicesPage();
        assertTrue(selenium.isTextPresent("Deployed"));
        String availableServices = "//a[@id='availableServicesLink']";
        assertTrue(selenium.isElementPresent(availableServices));
        clickAndWait(availableServices);
        String deployedLink = "//a[@id='deployedServicesLink']";
        assertTrue(selenium.isElementPresent(deployedLink));
        clickAndWait(deployedLink);
        assertTrue(selenium.isTextPresent("Deployed"));

    }
    
}
