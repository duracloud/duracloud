/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.retry;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Bill Branan
 *         Date: 10/25/13
 */
public class RetrierTest {

    @Test
    public void testRetrierSuccess() throws Exception {
        final String expectedResponse = "success!";
        String response = null;

        Retrier retrier = new Retrier();
        response = retrier.execute(new Retriable() {
            @Override
            public String retry() throws Exception {
              return expectedResponse;
            }
        });

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testRetrierRetry() throws Exception {
        final int expectedAttempts = 3;
        final int failuresBeforeSuccess = 2;
        final RetryTester retryTester = new RetryTester(failuresBeforeSuccess);

        Retrier retrier = new Retrier();
        int actualAttempts = retrier.execute(new Retriable() {
            @Override
            public Integer retry() throws Exception {
                return retryTester.doWork();
            }
        });

        assertEquals(expectedAttempts, actualAttempts);
    }

    @Test
    public void testRetrierFail() throws Exception {
        final Integer expectedAttempts = 4;
        final int failuresBeforeSuccess = 4; // More than the number of retries
        final RetryTester retryTester = new RetryTester(failuresBeforeSuccess);

        Retrier retrier = new Retrier();
        try {
            retrier.execute(new Retriable() {
                @Override
                public Integer retry() throws Exception {
                    return retryTester.doWork();
                }
            });
        } catch (Exception e) {
            assertEquals(expectedAttempts, Integer.valueOf(e.getMessage()));
        }
    }

    @Test
    public void testRetrierExponentiallyIncreasingWaitBetweenRetries() throws Exception {
        final Integer expectedAttempts = 4;
        final int failuresBeforeSuccess = 4; // More than the number of retries
        final RetryTester retryTester = new RetryTester(failuresBeforeSuccess);

        Retrier retrier = new Retrier(Retrier.DEFAULT_MAX_RETRIES, 100, 2);
        long start = System.currentTimeMillis();
        try {
            retrier.execute(new Retriable() {
                @Override
                public Integer retry() throws Exception {
                    return retryTester.doWork();
                }
            });
        } catch (Exception e) {
            assertEquals(expectedAttempts, Integer.valueOf(e.getMessage()));
            long elapsed = System.currentTimeMillis() - start;
            //0+1+4
            assertTrue(elapsed >= 500);
            assertTrue(elapsed < 800);

        }
    }

    
    private class RetryTester {
        private int failuresBeforeSuccess;
        private int attempts;

        public RetryTester(int failuresBeforeSuccess) {
            this.failuresBeforeSuccess = failuresBeforeSuccess;
            this.attempts = 0;
        }

        public Integer doWork() {
            attempts++;
            if(attempts <= failuresBeforeSuccess) {
                throw new RuntimeException(String.valueOf(attempts));
            }
            return attempts;
        }

        public int getAttempts() {
            return attempts;
        }
    }

}
