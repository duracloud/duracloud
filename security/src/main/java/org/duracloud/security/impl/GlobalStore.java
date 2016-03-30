/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;

import org.duracloud.account.db.model.AccountChangeEvent;
/**
 * 
 * @author Daniel Bernstein
 *
 * @param <T>
 */
public interface GlobalStore<T> {

    public T get(String accountId);
    
    void onEvent(AccountChangeEvent event);
}
