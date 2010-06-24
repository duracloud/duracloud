/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: May 21, 2010
 */
public class NoopTaskRunnerTest {

    @Test
    public void testNoopTaskRunner() {
        NoopTaskRunner noop = new NoopTaskRunner();

        String name = noop.getName();
        assertEquals("noop", name);

        String response = noop.performTask("");
        assertNotNull(response);
    }
}
