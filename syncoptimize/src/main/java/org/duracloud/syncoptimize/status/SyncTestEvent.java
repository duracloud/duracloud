/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.status;

/**
 * The result of a single SyncTool test action. The test takes place using a
 * specific number of threads, and a certain amount of time passes while the
 * test is running. That information is contained here.
 *
 * @author Bill Branan
 *         Date: 5/16/14
 */
public class SyncTestEvent {

    private static final Float MILLIS_IN_A_SEC = 1000f;

    private int threads;
    private long elapsed; // Elapsed time in milliseconds
    private float elapsedSeconds; // Elapsed time in seconds
    private float transferRate; // Transfer rate in Mbps

    public SyncTestEvent(int threads, long elapsed, int transferedMB) {
        this.threads = threads;
        this.elapsed = elapsed;
        this.elapsedSeconds = elapsed / MILLIS_IN_A_SEC;
        this.transferRate = (transferedMB * 8) / elapsedSeconds; // Mb per sec
    }

    public int getThreads() {
        return threads;
    }

    public long getElapsed() {
        return elapsed;
    }

    public float getTransferRate() {
        return transferRate;
    }

    @Override
    public String toString() {
        return "Test with " + threads + " threads required " + elapsedSeconds +
               " seconds. Transfer rate: " + transferRate + " Mbps.";
    }
}
