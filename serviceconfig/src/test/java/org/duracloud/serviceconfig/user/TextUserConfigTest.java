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

/**
 * @author Andrew Woods
 *         Date: Dec 5, 2010
 */
public class TextUserConfigTest {

    private TextUserConfig config;

    @Before
    public void setUp() {
        config = new TextUserConfig("name",
                                    "displayName",
                                    "value",
                                    "exclusion");
    }

    @Test
    public void testClone() throws Exception {
        verify(config, config, true);

        TextUserConfig clone = config.clone();
        verify(config, clone, true);
        clone.setValue("new-value");
        verify(config, clone, false);
    }

    private void verify(TextUserConfig source,
                        TextUserConfig clone,
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
            Assert.assertEquals(source.getValue(), clone.getValue());
            isValid = true;

        } catch (Throwable t) {
            Assert.assertFalse(valid);
        }
        Assert.assertEquals("expected validity [" + valid + "]",
                            valid,
                            isValid);
    }

}
