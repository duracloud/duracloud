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
 * @author Bill Branan
 *         Date: Jan 18, 2011
 */
public class ExtendedIteratorCounterThread implements Runnable {

    @SuppressWarnings("unchecked")
	private Iterator itr;
    private ExtendedCountListener listener;

    @SuppressWarnings("unchecked")
	public ExtendedIteratorCounterThread(Iterator itr, ExtendedCountListener listener) {
        this.itr = itr;
        this.listener = listener;
    }

    @Override
    public void run() {
        if(null != listener) {
            long count = 0;
            while (itr != null && itr.hasNext()) {
                listener.setIntermediaryCount(++count);
                itr.next();
            }

            listener.setCount(count);
            listener.setCountComplete();
        }
    }

}
