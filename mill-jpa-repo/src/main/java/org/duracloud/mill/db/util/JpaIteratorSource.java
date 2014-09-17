/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.mill.db.util;

import java.util.Collection;

import org.duracloud.common.collection.IteratorSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
/**
 * 
 * @author Daniel Bernstein
 *         Date: Sep 3, 2014
 * @param <R>
 * @param <T>
 */
public abstract class JpaIteratorSource<R, T> implements IteratorSource<T> {
    private int currentPage = 0;
    private int maxResults;
    private R repo;

    public JpaIteratorSource(R repo, int maxResults){
        this.repo = repo;
        if(maxResults <= 0){
            throw new IllegalArgumentException("maxResults must be greater than 0");
        }
        this.maxResults = maxResults;
    }

    public JpaIteratorSource(R repo){
        this(repo, 1000);
    }
    
    @Override
    public Collection<T> getNext() {
        if(currentPage < 0){
            return null;
        }
        Page<T> page =  getNextPage(new PageRequest(currentPage, maxResults), repo);
        currentPage++;
        if(page.getTotalPages() == currentPage){
            currentPage = -1;
        }
        
        return page.getContent();
        
    }
    
    /**
     * Specific Jpa call goes here.
     * @param pageable
     * @return
     */
    protected abstract Page<T> getNextPage(Pageable pageable, R repo);

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
}
