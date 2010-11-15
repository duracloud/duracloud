/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.j2kservice;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Nov 15, 2010
 */
public class J2kWebappWrapperTest {

    private J2kWebappWrapper j2k;

    @Before
    public void setUp() throws Exception {
        j2k = new J2kWebappWrapper();
    }

    @Test
    public void testGetPlatform() throws Exception {
        String platform = j2k.getPlatform();
        Assert.assertNotNull(platform);

        String localPlatform = getLocalPlatform();
        if (localPlatform.contains("Window")) {
            Assert.assertEquals("Win32", platform);

        } else if (localPlatform.contains("Mac")) {
            Assert.assertEquals("Mac-x86", platform);

        } else if (localPlatform.contains("Linux")) {
            String localArch = getLocalArch();
            if (localArch.equals("32")) {
                Assert.assertEquals("Linux-x86-32", platform);

            } else if (localArch.equals("64")) {
                Assert.assertEquals("Linux-x86-64", platform);

            } else {
                Assert.fail("Unexpected arch name: '" + localArch + "'");
            }

        } else {
            Assert.fail("Unexpected platform name: '" + localPlatform + "'");
        }
    }

    private String getLocalPlatform() {
        String localPlatform = System.getProperty("os.name");
        Assert.assertNotNull(localPlatform);

        return localPlatform;
    }

    private String getLocalArch() {
        String localArch = System.getProperty("sun.arch.data.model");
        Assert.assertNotNull(localArch);

        return localArch;
    }
}
