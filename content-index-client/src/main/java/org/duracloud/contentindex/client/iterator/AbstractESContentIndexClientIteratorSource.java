/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.client.iterator;

import org.duracloud.common.collection.IteratorSource;
import org.duracloud.contentindex.client.ESContentIndexClient;

import java.util.Collection;

/**
 * @author Erik Paulsson
 *         Date: 5/8/14
 */
public abstract class AbstractESContentIndexClientIteratorSource<T> implements IteratorSource<T> {

    protected ESContentIndexClient contentIndexClient;
    protected int pageNum = 0;
    protected String account;
    protected String storeId;
    protected String space;

    public AbstractESContentIndexClientIteratorSource(
        ESContentIndexClient contentIndexClient, String account, String storeId,
        String space) {
        this.contentIndexClient = contentIndexClient;
        this.account = account;
        this.storeId = storeId;
        this.space = space;
    }

    public abstract Collection<T> getNext();
}
