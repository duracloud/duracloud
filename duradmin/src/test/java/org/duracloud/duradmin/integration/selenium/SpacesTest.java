/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.integration.selenium;

/**
 * @author Daniel Bernstein
 */
public class SpacesTest extends SpaceTestBase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void testBasic() throws Exception {
        navigateToSpacesPage();
    }
}
