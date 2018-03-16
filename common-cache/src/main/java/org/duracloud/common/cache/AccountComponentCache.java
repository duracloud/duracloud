/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.cache;

import org.duracloud.common.event.AccountChangeEvent;

/**
 * An interface for accessing cached components associated with an account.
 *
 * @param <T>
 * @author Daniel Bernstein
 */
public interface AccountComponentCache<T> {

    public T get(String accountId);

    void onEvent(AccountChangeEvent event);
}
