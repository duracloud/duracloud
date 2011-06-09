/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonfixity.postprocessing;

import org.duracloud.common.util.bulk.ManifestVerifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.duracloud.common.util.bulk.ManifestVerifier.State;

/**
 * @author Andrew Woods
 *         Date: 6/9/11
 */
public class FixityPassFailPostJobWorkerTest {

    private FixityPassFailPostJobWorker worker;

    @Before
    public void setUp() throws Exception {
        worker = new FixityPassFailPostJobWorker(null, null, null, null, null);
    }

    @Test
    public void test() {
        testIsError(true, "hello");
        testIsError(true, "xx,MISMATCH,yy");

        testIsError(false, "xx,VALID,yy");
        testIsError(false, null);
        testIsError(false, "");
        testIsError(false, "         ");
        testIsError(false, System.getProperty("line.separator"));

        for (ManifestVerifier.State state : State.values()) {
            testIsError(state != State.VALID, "xx,yy," + state);
        }
    }

    private void testIsError(boolean expected, String line) {
        Assert.assertEquals(line, expected, worker.isError(line));
    }
}
