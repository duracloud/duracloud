package org.duracloud.security.impl;

public interface GlobalStore {

    void remove(String key);

    void removeAll();

}
