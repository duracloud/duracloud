/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Dec 4, 2010
 */
public class DeploymentOptionTest {

    private DeploymentOption option;

    @Before
    public void setUp() {
        option = new DeploymentOption();
        option.setDisplayName("displayName");
        option.setHostname("hostname");
        option.setLocation(DeploymentOption.Location.EXISTING);
        option.setState(DeploymentOption.State.AVAILABLE);
    }

    @Test
    public void testClone() throws Exception {
        verify(option, option, true);

        DeploymentOption clone = option.clone();
        verify(option, clone, true);
        clone.setDisplayName("new-name");
        verify(option, clone, false);

        clone = option.clone();
        verify(option, clone, true);
        clone.setHostname("new-host-name");
        verify(option, clone, false);

        clone = option.clone();
        verify(option, clone, true);
        clone.setLocation(DeploymentOption.Location.NEW);
        verify(option, clone, false);

        clone = option.clone();
        verify(option, clone, true);
        clone.setState(DeploymentOption.State.UNAVAILABLE);
        verify(option, clone, false);
    }

    private void verify(DeploymentOption source,
                        DeploymentOption clone,
                        boolean valid) {
        boolean isValid = false;
        try {
            Assert.assertEquals(source.getDisplayName(),
                                clone.getDisplayName());
            Assert.assertEquals(source.getHostname(), clone.getHostname());
            Assert.assertEquals(source.getLocation(), clone.getLocation());
            Assert.assertEquals(source.getState(), clone.getState());
            isValid = true;

        } catch (Throwable t) {
            Assert.assertFalse(valid);
        }
        Assert.assertEquals("expected validity [" + valid + "]",
                            valid,
                            isValid);
    }
}
