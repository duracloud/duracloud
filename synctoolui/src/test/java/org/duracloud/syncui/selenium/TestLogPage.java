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
public class TestLogPage extends BasePostSetupPage {

    @Test
    public void testGet() {
        sc.open(getAppRoot() + "/log");
        Assert.assertTrue(isElementPresent("id=log"));
        
    }
}
