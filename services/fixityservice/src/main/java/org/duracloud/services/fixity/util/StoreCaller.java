/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.util;

import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class spins on the abstract 'call()' until it returns successfully
 * or it has run out of tries.
 *
 * @author Andrew Woods
 *         Date: Aug 6, 2010
 */
public abstract class StoreCaller<T> {

    private final Logger log = LoggerFactory.getLogger(StoreCaller.class);

    public T call() {
        T result = null;
        boolean callComplete = false;
        int maxTries = 3;
        int tries = 0;

        while (!callComplete && tries < maxTries) {
            try {
                result = doCall();
                callComplete = true;
            } catch (ContentStoreException e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    // do nothing
                }
                log.warn(getLogMessage());
                tries++;
            }
        }
        return result;
    }

    protected abstract T doCall() throws ContentStoreException;

    public abstract String getLogMessage();
}
