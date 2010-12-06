/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Dec 5, 2010
 */
public class SystemConfigTest {

    private SystemConfig systemConfig;

    @Before
    public void setUp() {
        systemConfig = new SystemConfig("name", "value", "defaultValue");
        systemConfig.setId(10);
    }

    @Test
    public void testClone() throws Exception {
        verify(systemConfig, systemConfig, true);

        SystemConfig clone = systemConfig.clone();
        verify(systemConfig, clone, true);
        clone.setId(34);
        verify(systemConfig, clone, false);

        clone = systemConfig.clone();
        verify(systemConfig, clone, true);
        clone.setValue("new-value");
        verify(systemConfig, clone, false);
    }

    private void verify(SystemConfig source,
                        SystemConfig clone,
                        boolean valid) {
        boolean isValid = false;
        try {
            Assert.assertEquals(source.getDefaultValue(),
                                clone.getDefaultValue());
            Assert.assertEquals(source.getId(), clone.getId());
            Assert.assertEquals(source.getName(), clone.getName());
            Assert.assertEquals(source.getValue(), clone.getValue());
            isValid = true;

        } catch (Throwable t) {
            Assert.assertFalse(t.getMessage(), valid);
        }
        Assert.assertEquals("expected validity [" + valid + "]",
                            valid,
                            isValid);
    }

}
