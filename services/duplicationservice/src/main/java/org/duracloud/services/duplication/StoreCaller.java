/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication;

import org.duracloud.services.duplication.error.DuplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * This class spins on the abstract doCall() until the expected
 * result is returned or the maximum number of tries has been reached.
 *
 * @author Andrew Woods
 *         Date: Jan 21, 2011
 */
public abstract class StoreCaller<T> {
    private static final Logger log =
        LoggerFactory.getLogger(StoreCaller.class);

    private final int waitMillis;

    public StoreCaller(int waitMillis) {
        this.waitMillis = waitMillis;
    }

    public T call() {
        String error = null;
        boolean callComplete = false;
        int maxTries = 4;
        int tries = 0;

        T result = null;
        while (!callComplete && tries < maxTries) {
            try {
                result = doCall();
                callComplete = true;

            } catch (Exception e) {
                sleep(tries);
                callComplete = false;
                error = "Exception in retry call: " + e.getMessage();
                log.warn(error);
            }
            tries++;
        }

        if (!callComplete) {
            throw new DuplicationException(error);
        }

        return result;
    }

    protected abstract T doCall() throws Exception;

    private void sleep(int tries) {
        Random random = new Random();
        long millis = random.nextInt(waitMillis) * (long) Math.pow(3, tries);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing.
        }
    }
}