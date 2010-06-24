/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.j2kservice.osgi;

import org.duracloud.common.util.ExceptionUtil;
import org.duracloud.services.j2kservice.J2kWebappWrapper;
import org.junit.Assert;

import java.io.IOException;

/**
 * @author Andrew Woods
 *         Date: Dec 20, 2009
 */
public class J2kWebappWrapperTester extends J2kWebappWrapperTestBase {

    private int port = 18080;

    public J2kWebappWrapperTester(J2kWebappWrapper wrapper) throws IOException {
        this.wrapper = wrapper;
    }

    protected void testDjatokaWebappWrapper() throws Exception {
        Throwable error = null;
        try {
            super.testStopStartCycle(getNextRunningUrl());
            super.testImageServing(getNextRunningUrl());
        } catch (Throwable e) {
            error = e;
        } finally {
            doTearDown();
        }

        if (error != null) {
            StringBuilder msg = new StringBuilder("err: " + error.getMessage());
            msg.append("\n");
            msg.append(ExceptionUtil.getStackTraceAsString(error));
            Assert.fail(msg.toString());
        }
    }

    private void doTearDown() {
        try {
            wrapper.stop();
        } catch (Exception e) {
            // do nothing.
        }
    }

    private String getNextRunningUrl() {
        String url = urlRunningBase + ":" + port + "/" + context;
        port++;
        return url;
    }

}
