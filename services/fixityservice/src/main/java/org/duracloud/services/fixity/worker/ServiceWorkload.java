/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.worker;

import org.duracloud.services.fixity.util.CountListener;

/**
 * This class is responsible for determining the set of items that should be
 * worked.
 *
 * @author Andrew Woods
 *         Date: Aug 4, 2010
 */
public interface ServiceWorkload<T> {

    public boolean hasNext();

    public T next();

    public void registerCountListener(CountListener listener);
}
