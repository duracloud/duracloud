/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util;

import org.junit.Assert;
import org.junit.Test;
import org.duracloud.servicesutil.util.catalog.BundleCatalog;

/**
 * @author Andrew Woods
 *         Date: Dec 11, 2009
 */
public class BundleCatalogTest {

    @Test
    public void testRegistrationCycle() {
        String name0 = "name0.jar";
        String name1 = "name1.jar";
        String name2 = "name2.jar";
        String name3 = "name3.jar";
        String name4 = "name4.jar";

        Assert.assertTrue(BundleCatalog.register(name0));
        Assert.assertTrue(BundleCatalog.register(name1));
        Assert.assertTrue(BundleCatalog.register(name2));
        Assert.assertTrue(BundleCatalog.register(name3));
        Assert.assertTrue(BundleCatalog.register(name4));

        Assert.assertFalse(BundleCatalog.register(name0));
        Assert.assertFalse(BundleCatalog.register(name1));
        Assert.assertFalse(BundleCatalog.register(name2));
        Assert.assertFalse(BundleCatalog.register(name3));
        Assert.assertFalse(BundleCatalog.register(name4));

        Assert.assertFalse(BundleCatalog.unRegister(name0));
        Assert.assertFalse(BundleCatalog.unRegister(name1));
        Assert.assertFalse(BundleCatalog.unRegister(name2));
        Assert.assertFalse(BundleCatalog.unRegister(name3));
        Assert.assertFalse(BundleCatalog.unRegister(name4));

        Assert.assertTrue(BundleCatalog.unRegister(name0));
        Assert.assertTrue(BundleCatalog.unRegister(name1));
        Assert.assertTrue(BundleCatalog.unRegister(name2));
        Assert.assertTrue(BundleCatalog.unRegister(name3));
        Assert.assertTrue(BundleCatalog.unRegister(name4));

        expectException(name0);

    }

    @Test
    public void testNameMatching() {
        // unique
        String name0 = "name.jar";
        String name1 = "name-1.0.0.jar";
        String name2 = "name-1.0.2.jar";
        String name3 = "dir/name-1.4.0.jar";

        // duplicates
        String name4 = "dir/name.jar";
        String name5 = "dirX/dirY/name-1.0.0.jar";

        // errors
        String name6 = "dir/name";
        String name7 = "name.war";
        String name8 = "name-1.0.0";
        
        Assert.assertTrue(BundleCatalog.register(name0));
        Assert.assertTrue(BundleCatalog.register(name1));
        Assert.assertTrue(BundleCatalog.register(name2));
        Assert.assertTrue(BundleCatalog.register(name3));

        Assert.assertFalse(BundleCatalog.register(name4));
        Assert.assertFalse(BundleCatalog.register(name5));

        expectException(name6);
        expectException(name7);
        expectException(name8);

        Assert.assertFalse(BundleCatalog.unRegister(name4));
        Assert.assertFalse(BundleCatalog.unRegister(name5));

        Assert.assertTrue(BundleCatalog.unRegister(name0));
        Assert.assertTrue(BundleCatalog.unRegister(name1));
        Assert.assertTrue(BundleCatalog.unRegister(name2));
        Assert.assertTrue(BundleCatalog.unRegister(name3));

    }

    private void expectException(String name) {
        boolean exception = false;
        try {
            BundleCatalog.unRegister(name);
            Assert.fail("Exception expected: "+name);
        } catch (Exception e) {
            exception = true;
        }
        Assert.assertTrue(name, exception);
    }

}
