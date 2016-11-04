/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.retry;

import org.duracloud.common.util.WaitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles performing actions that need to be retried.
 *
 * Expected usage example (using lambda):
 *
 * Retrier retrier = new Retrier();
 * return retrier.execute(() -> {
 *     // The actual method being executed
 *     return doWork();
 * });
 *
 * Older style usage, this is equivalent to the above example:
 *
 * Retrier retrier = new Retrier();
 * return retrier.execute(new Retriable() {
 *     @Override
 *     public String retry() throws Exception {
 *         // The actual method being executed
 *         return doWork();
 *     }
 * });
 *
 * @author Bill Branan
 *         Date: 10/23/13
 */
public class Retrier {

    /**
     * Default max number of attempts to make in performing a Retriable action
     */
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final int DEFAULT_WAIT_BETWEEN_RETRIES = 1000;
    public static final int DEFAULT_WAIT_MULTIPLIER = 1;
    private int maxRetries;
    
    /**
     * The number of milliseconds to wait between retries
     */
    private long waitBetweenRetries;

    /**
     * A multiplier to make waits between retries increase exponentially. The wait time will 
     * for each retry equals  (attempt ^ multiplier) * wait
     */
    private int waitBetweenRetriesMultiplier;

    private static final Logger log = LoggerFactory.getLogger(Retrier.class);
    
    public Retrier() {
        this(DEFAULT_MAX_RETRIES,
             DEFAULT_WAIT_BETWEEN_RETRIES,
             DEFAULT_WAIT_MULTIPLIER);
    }

    /**
     * 
     * @param maxRetries
     */
    public Retrier(int maxRetries){
        this(maxRetries, DEFAULT_WAIT_BETWEEN_RETRIES, DEFAULT_WAIT_MULTIPLIER);
    }

    /**
     * 
     * @param maxRetries
     * @param waitBetweenRetries
     * @param waitBetweenRetriesMultiplier
     */
    public Retrier(int maxRetries, int waitBetweenRetries, int waitBetweenRetriesMultiplier) {
        this.maxRetries = maxRetries;
        this.waitBetweenRetries = waitBetweenRetries;
        this.waitBetweenRetriesMultiplier = waitBetweenRetriesMultiplier;
    }
    
    private static final ExceptionHandler DEFAULT_EXCEPTION_HANDLER =
        new ExceptionHandler() {
        @Override
        public void handle(Exception ex) {
            log.debug(ex.getMessage(),ex);
        }
    };

    /**
     * Executes the action of a Retriable, and retries on error. Provides a
     * way to execute a variety of methods and allow a retry to occur on
     * method failure.
     *
     * Returns the necessary object type.
     *
     * This method, along with the Retriable interface is an implementation
     * of the command pattern.
     *
     * @param retriable
     * @throws Exception
     */
    public <T extends Object> T execute(Retriable retriable) throws Exception {
        return execute(retriable, DEFAULT_EXCEPTION_HANDLER);
    }

    /**
     * Executes the action of a Retriable, and retries on error. Provides a way
     * to execute a variety of methods and allow a retry to occur on method
     * failure.
     * 
     * Returns the necessary object type.
     * 
     * @param retriable
     * @param exceptionHandler
     *            for customing handling of exceptions - especially with respect
     *            to customized logging.
     * @throws Exception
     */
    public <T extends Object> T execute(Retriable retriable,
                                        ExceptionHandler exceptionHandler)
        throws Exception {

        if (exceptionHandler == null) {
            throw new IllegalArgumentException(
                    "exceptionHandler must be non-null");
        }
        
        Exception lastException = null;
        for(int i=0; i<=maxRetries; i++) {
            try {
                return (T)retriable.retry();
            } catch (Exception e) {
                lastException = e;
                exceptionHandler.handle(e);
                if(i <= maxRetries){
                    WaitUtil.waitMs((long)Math.pow(i,waitBetweenRetriesMultiplier)*waitBetweenRetries);
                }
            }
        }
        throw lastException;
    }
}
