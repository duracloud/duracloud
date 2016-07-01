/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Daniel Bernstein
 *
 * @param <T>
 */
public abstract class AbstractAccountComponentCache<T> implements AccountComponentCache<T> {
    private Map<String, T> cache;

    public AbstractAccountComponentCache() {
        this.cache = new HashMap<>();
    }

    protected void remove(String key) {
        this.cache.remove(key);
    }

    protected void removeAll() {
        this.cache.clear();
    }

    @Override
    public T get(String accountId) {
        T instance = this.cache.get(accountId);
        if (instance == null) {
            instance = createInstance(accountId);
            this.cache.put(accountId, instance);
        }

        return instance;
    }

    protected abstract T createInstance(String accountId);

}
