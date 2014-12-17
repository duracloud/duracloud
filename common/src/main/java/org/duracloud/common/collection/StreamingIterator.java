/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * An {code Iterator} implementation that delegates its underlying data provider to an 
 * {code IteratorSource}.  It is useful for implementing an iterator that iterates over
 * a collection of items of indeterminate size. 
 * @author Daniel Bernstein
 * 
 * @param <T>
 */
public class StreamingIterator<T> implements Iterator<T>{

    private Queue<T> queue = new LinkedList<T>();
    
    private IteratorSource<T> source;
    
    public StreamingIterator(IteratorSource<T> source){
        this.source = source;
    }
    
    @Override
    public synchronized boolean hasNext() {
        if(!queue.isEmpty()){
            return true;
        } 
        
        Collection<T> chunk = source.getNext();
        if(chunk != null){
            Iterator<T> it = chunk.iterator();
            while(it.hasNext()){
                T item = it.next();
                queue.add(item);
            }
        }
        
        if(queue.size() > 0){
            return true;
        }
        
        return false;
    }

    @Override
    public T next() {
        if(hasNext()){
            return queue.remove();
        }
        
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
