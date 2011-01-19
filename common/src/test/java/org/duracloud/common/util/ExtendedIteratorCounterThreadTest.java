/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bill Branan
 *         Date: Jan 18, 2011
 */
public class ExtendedIteratorCounterThreadTest implements ExtendedCountListener {

    private ExtendedIteratorCounterThread counter;
    private ExtendedCountListener listener;
    private List<String> list;
    private final int NUM_ELEMENTS = 11000;

    private long count = 0;
    private long interCount = 0;
    private boolean countComplete = false;

    @Before
    public void setUp() {
        list = new ArrayList<String>();
        for (int i = 0; i < NUM_ELEMENTS; ++i) {
            list.add("x");
        }

        listener = this;
        counter = new ExtendedIteratorCounterThread(list.iterator(), listener);
    }

    @Test
    public void testRun() throws Exception {
        Assert.assertEquals(0, count);

        Thread p = new Thread(counter);
        p.start();
        p.join();

        Assert.assertEquals(NUM_ELEMENTS, count);
        Assert.assertEquals(interCount, count);
        Assert.assertTrue(countComplete);
    }

    @Override
    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public void setIntermediaryCount(long count) {
        this.interCount = count;
    }

    @Override
    public void setCountComplete() {
        this.countComplete = true;
    }

}
