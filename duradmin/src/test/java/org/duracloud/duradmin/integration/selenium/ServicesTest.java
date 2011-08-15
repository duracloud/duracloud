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
public class ServicesTest
        extends ServicesTestBase{

    public void setUp() throws Exception {
        super.setUp();
        
    }
    
    public void test() throws Exception{
        navigateToServicesPage();
    }
    
}
