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
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Oct 24, 2009
 */
public class DuraCloudExceptionTest {

    private final String key0 = "test.element";
    private final String key1 = "test.does.not.exist";

    private final String val0 = "hello";

    @Before
    public void setUp() {
        ExceptionMessages.setConfigFileName("test.properties");
    }

    @Test
    public void testGetFormatedMessagePattern() {
        DuraCloudCheckedException e = createException(key0);
        assertNotNull(e);

        String msg = e.getFormattedMessage();
        assertNotNull(msg);
        assertEquals(val0, msg);
    }

    @Test
    public void testGetFormatedMessageStack() {
        DuraCloudCheckedException e = createException(key1);
        assertNotNull(e);

        String msg = e.getFormattedMessage();
        assertNotNull(msg);
        assertTrue(!val0.equals(msg));
    }

    private DuraCloudCheckedException createException(String key) {
        DuraCloudCheckedException e = null;
        try {
            Integer.parseInt("junk");
        } catch (NumberFormatException nfe) {
            e = new DuraCloudCheckedException(nfe, key);
        }
        return e;
    }

}
