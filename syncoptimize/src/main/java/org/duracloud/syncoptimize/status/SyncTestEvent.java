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

    private int threads;
    private long elapsed;

    public SyncTestEvent(int threads, long elapsed) {
        this.threads = threads;
        this.elapsed = elapsed;
    }

    public int getThreads() {
        return threads;
    }

    public long getElapsed() {
        return elapsed;
    }

}
