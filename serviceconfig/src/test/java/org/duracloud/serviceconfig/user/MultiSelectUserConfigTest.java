/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.user;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Dec 5, 2010
 */
public class MultiSelectUserConfigTest {

    private MultiSelectUserConfig config;

    @Before
    public void setUp() {
        config = new MultiSelectUserConfig("name",
                                           "displayName",
                                           createOptions());
    }

    private List<Option> createOptions() {
        return new ArrayList<Option>();
    }

    @Test
    public void testClone() throws Exception {
        verify(config, config, true);

        MultiSelectUserConfig clone = config.clone();
        verify(config, clone, true);
    }

    private void verify(MultiSelectUserConfig source,
                        MultiSelectUserConfig clone,
                        boolean valid) {
        boolean isValid = false;
        try {
            Assert.assertEquals(source.getDisplayName(),
                                clone.getDisplayName());
            Assert.assertEquals(source.getDisplayValue(),
                                clone.getDisplayValue());
            Assert.assertEquals(source.getExclusion(), clone.getExclusion());
            Assert.assertEquals(source.getId(), clone.getId());
            Assert.assertEquals(source.getInputType(), clone.getInputType());
            Assert.assertEquals(source.getName(), clone.getName());
            Assert.assertEquals(source.getOptions(), clone.getOptions());
            isValid = true;

        } catch (Throwable t) {
            Assert.assertFalse(t.getMessage(), valid);
        }
        Assert.assertEquals("expected validity [" + valid + "]",
                            valid,
                            isValid);
    }

}
