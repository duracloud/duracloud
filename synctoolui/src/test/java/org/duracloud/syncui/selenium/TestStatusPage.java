/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.selenium;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class TestStatusPage extends BasePostSetupPage {

    @Test
    public void testGet() {
        sc.open(getAppRoot() + "/status");
        Assert.assertTrue(isElementPresent("id=active-syncs"));
        Assert.assertTrue(isElementPresent("id=queued"));
        Assert.assertFalse(isElementPresent("id=errors"));
        clickAndWait("css=#errors-tab a");
        Assert.assertTrue(isElementPresent("id=errors"));
        Assert.assertFalse(isElementPresent("id=queued"));
        
    }

    @Test
    public void testGetErrorsTabActivated() {
        sc.open(getAppRoot() + "/status?statusTab=errors");
        Assert.assertTrue(isElementPresent("id=errors"));

    }

}
