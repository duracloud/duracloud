/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.integration.selenium;



public class AddSpacesTest
        extends SpaceTestBase{

    
    public void setUp() throws Exception {
        super.setUp();
        navigateToSpacesPage();
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddAndRemoveSpace() throws Exception {
        String spaceId = addSpace();
        removeSpace(spaceId);

    }
}
