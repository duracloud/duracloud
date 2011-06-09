/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.postprocessing;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: 6/9/11
 */
public class SimplePassFailPostJobWorkerTest {

    private SimplePassFailPostJobWorker worker;

    @Before
    public void setUp() throws Exception {
        worker = new SimplePassFailPostJobWorker(null, null, null, null, null);
    }

    @Test
    public void test() {
        testIsError(true, "hello");
        testIsError(true, "xx,success,yy");

        testIsError(false, "success,xx,yy");
        testIsError(false, null);
        testIsError(false, "");
        testIsError(false, "         ");
        testIsError(false, System.getProperty("line.separator"));
    }

    private void testIsError(boolean expected, String line) {
        Assert.assertEquals(line, expected, worker.isError(line));
    }

}
