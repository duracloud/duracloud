/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import java.lang.reflect.Method;

import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StatelessStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.core.Ordered;

/**
 * Verifies that when a space is created it is available for access
 * prior to returning.
 *
 * Assumes that the breakpoint for this advice is on method
 * StatelessStorageProvider.createSpace(String storeId, String spaceId)
 */
public class VerifySpaceCreationAdvice
        implements AfterReturningAdvice, Ordered {

    private final Logger log = LoggerFactory.getLogger(VerifySpaceCreationAdvice.class);

    private int maxRetries;

    private int waitTime;

    private int order;

    public void afterReturning(Object returnObj,
                               Method method,
                               Object[] methodArgs,
                               Object targetObj) throws Throwable {
        boolean spaceExists = false;
        int numAttempts = 0;

        StatelessStorageProvider provider = (StatelessStorageProvider)targetObj;
        StorageProvider target = (StorageProvider)methodArgs[0];
        String storeId = (String)methodArgs[1];
        String spaceId = (String)methodArgs[2];

        do {
            numAttempts++;
            try {
                // Simple test to determine if a space exists
                provider.getSpaceMetadata(target, storeId, spaceId);
                spaceExists = true;
            } catch(StorageException se) {
                if (log.isDebugEnabled()) {
                    log.debug("Attempt " + numAttempts + " to verify " +
                              "that space " + spaceId +
                              " exists after creation failed.");
                }
                Thread.sleep(waitTime);
            }
        } while(!spaceExists && (numAttempts <= maxRetries));

        if(!spaceExists) {
            String error = "Unable to verify creation of space " +
                           spaceId + " after " +
                           maxRetries + " attempts.";
            throw new StorageException(error, StorageException.RETRY);
        }
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
