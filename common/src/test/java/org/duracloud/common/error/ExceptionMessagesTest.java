/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.error;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

public class ExceptionMessagesTest {

    private final String key0 = "test.element";
    private final String key1 = "test.does.not.exist";

    private final String val0 = "hello";

    @Before
    public void setUp() {
        ExceptionMessages.setConfigFileName("test.properties");
    }

    @Test
    public void testGetMessagePattern() throws Exception {
        String pattern = ExceptionMessages.getMessagePattern(key0);
        assertNotNull(pattern);
        assertEquals(val0, pattern);
    }

    @Test
    public void testGetMessagePatternBad() throws Exception {
        String pattern = ExceptionMessages.getMessagePattern(key1);
        assertEquals(null, pattern);
    }

}