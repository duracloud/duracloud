/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 * Date: Aug 10, 2010
 */
public class IteratorCounterThreadTest implements CountListener {

    private IteratorCounterThread counter;
    private CountListener listener;
    private List<String> list;
    private final int NUM_ELEMENTS = 11000;

    private long count = 0;

    @Before
    public void setUp() {
        list = new ArrayList<String>();
        for (int i = 0; i < NUM_ELEMENTS; ++i) {
            list.add("x");
        }

        listener = this;
        counter = new IteratorCounterThread(list.iterator(), listener);
    }

    @Test
    public void testRun() throws Exception {
        Assert.assertEquals(0, count);

        Thread p = new Thread(counter);
        p.start();
        p.join();

        Assert.assertEquals(NUM_ELEMENTS, count);
    }

    @Override
    public void setCount(long count) {
        this.count = count;
    }
}
