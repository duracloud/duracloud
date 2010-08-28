/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.util.Iterator;

/**
 * This class spins through the arg Iterator and notifies the arg Listener
 * of the total count upon reaching the iteration end.
 *
 * @author Andrew Woods
 *         Date: Aug 10, 2010
 */
public class IteratorCounterThread implements Runnable {

    @SuppressWarnings("unchecked")
	private Iterator itr;
    private CountListener listener;

    @SuppressWarnings("unchecked")
	public IteratorCounterThread(Iterator itr, CountListener listener) {
        this.itr = itr;
        this.listener = listener;
    }

    @Override
    public void run() {
        long count = 0;
        while (itr != null && itr.hasNext()) {
            count++;
            itr.next();
        }

        if (listener != null) {
            listener.setCount(count);
        }
    }

}
