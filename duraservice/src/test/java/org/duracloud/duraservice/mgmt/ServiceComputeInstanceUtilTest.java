/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.mgmt;

import org.duracloud.duraservice.domain.ServiceComputeInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Apr 1, 2010
 */
public class ServiceComputeInstanceUtilTest {

    private ServiceComputeInstanceUtil util;

    @Before
    public void setUp() {
        util = new ServiceComputeInstanceUtil();
    }

    @After
    public void tearDown() {
        util = null;
    }

    @Test
    public void testCreateComputeInstance() {
        String hostName = null;
        String port = null;
        String context = null;
        String displayName = null;

        boolean valid = false;
        doTest(valid, hostName, port, context, displayName);

        valid = false;
        doTest(valid, "", "", "", "");

        valid = false;
        doTest(valid, null, "port", "context", "displayName");

        valid = false;
        doTest(valid, "hostName", "port", "context", null);

        valid = false;
        doTest(valid, "hostName", "port", "", "displayName");

        valid = true;
        doTest(valid, "hostName", "port", "context", "displayName");
    }

    private void doTest(boolean valid,
                        String hostName,
                        String port,
                        String context,
                        String displayName) {

        ServiceComputeInstance instance = null;
        boolean thrown = false;
        try {
            instance = util.createComputeInstance(hostName,
                                                  port,
                                                  context,
                                                  displayName);
            Assert.assertTrue(valid);
        } catch (Exception e) {
            thrown = true;
        }
        Assert.assertEquals(valid, !thrown);

        if (valid) {
            Assert.assertNotNull(instance);
        }
    }
}
