/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.web;

import org.duracloud.common.error.DuraCloudCheckedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a utility for monitoring the state of network resources.
 *
 * @author Andrew Woods
 *         Date: Jan 01, 2009
 */
public class NetworkUtil {

    protected static final Logger log = LoggerFactory.getLogger(NetworkUtil.class);

    public void waitForStartup(String url)
        throws DuraCloudCheckedException {
        isRunning(url, true);
    }

    public void waitForShutdown(String url)
        throws DuraCloudCheckedException {
        isRunning(url, false);
    }

    private void isRunning(String url, boolean state)
        throws DuraCloudCheckedException {
        int tries = 0;
        int maxTries = 40;
        boolean running = isRunning(url);
        while (running != state && tries++ < maxTries) {
            sleep(1000);
            running = isRunning(url);
        }

        if (running != state) {
            sleep(5000);
            running = isRunning(url);
        }

        if (running != state) {
            String err = state ? "Not running" : "Still running";
            throw new DuraCloudCheckedException(err + ": " + url);
        }
    }

    private boolean isRunning(String url) {
        boolean running = false;

        RestHttpHelper httpHelper = new RestHttpHelper();
        RestHttpHelper.HttpResponse response = null;
        try {
            response = httpHelper.get(url);
        } catch (Exception e) {
            // do nothing.
        }

        if (response != null) {
            int status = response.getStatusCode();
            if (status == 200 || status == 302) {
                running = true;

            } else {
                log.debug("status code: {}", status);
            }
        }

        return running;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing.
        }
    }

}
