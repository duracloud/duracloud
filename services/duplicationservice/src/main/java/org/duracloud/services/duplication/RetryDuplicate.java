/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication;

import org.duracloud.error.ContentStoreException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class spins on the abstract doReplicate() until the expected
 * result is returned or the maximum number of tries has been reached.
 *
 */
public abstract class RetryDuplicate {
    private static final Logger log = LoggerFactory.getLogger(
            RetryDuplicate.class);

    public boolean replicate() {
        boolean callComplete = false;
        int maxTries = 3;
        int tries = 0;

        while (!callComplete && tries < maxTries) {
            try {
                doReplicate();
                callComplete = true;

            } catch (ContentStoreException cse) {
                sleep(tries);
                callComplete = false;

                log.warn("Exception in replicate call: " + cse.getMessage() +
                    ", retry: " + !callComplete);

            } catch (Exception e) {
                log.warn("Unexpected exception: " + e.getMessage() +
                    ", retry: " + !callComplete);
                callComplete = false;
            }
            tries++;
        }

        return callComplete;
    }

    protected abstract void doReplicate() throws Exception;

    private static void sleep(int tries) {
        try {
            Thread.sleep((long) (Math.random() * (Math.pow(3, tries) * 10L)));
        } catch (InterruptedException e) {
            // do nothing.
        }
    }
}