/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.worker;

import org.duracloud.services.fixity.results.ServiceResultProcessor;

/**
 * Implementations of this class have the responsibility of creating new,
 * Runnable worker objects with a specific item to process.
 *
 * @author Andrew Woods
 *         Date: Aug 4, 2010
 */
public interface ServiceWorkerFactory<T> {

    public Runnable newWorker(T workItem);

}
