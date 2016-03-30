package org.duracloud.security.impl;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGlobalAccountStore<T> implements GlobalStore<T> {
    private Map<String, T> cache;

    public AbstractGlobalAccountStore() {
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
