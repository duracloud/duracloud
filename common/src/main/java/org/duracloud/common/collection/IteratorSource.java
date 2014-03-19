package org.duracloud.common.collection;

import java.util.Collection;

public interface IteratorSource<T> {
    public Collection<T> getNext();
}
