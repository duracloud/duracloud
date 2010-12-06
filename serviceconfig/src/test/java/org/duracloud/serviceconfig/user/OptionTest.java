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
public class OptionTest {

    private Option option;

    @Before
    public void setUp() {
        option = new Option("displayName", "value", true);
    }

    @Test
    public void testClone() throws Exception {
        verify(option, option, true);

        Option clone = option.clone();
        verify(option, clone, true);
        clone.setDisplayName("new-display-name");
        verify(option, clone, false);

        clone = option.clone();
        verify(option, clone, true);
        clone.setSelected(false);
        verify(option, clone, false);

        clone = option.clone();
        verify(option, clone, true);
        clone.setValue("new-value");
        verify(option, clone, false);
    }

    private void verify(Option source, Option clone, boolean valid) {
        boolean isValid = false;
        try {
            Assert.assertEquals(source.getDisplayName(),
                                clone.getDisplayName());
            Assert.assertEquals(source.getValue(), clone.getValue());
            Assert.assertEquals(source.isSelected(), clone.isSelected());
            isValid = true;

        } catch (Throwable t) {
            Assert.assertFalse(t.getMessage(), valid);
        }
        Assert.assertEquals("expected validity [" + valid + "]",
                            valid,
                            isValid);
    }

}
