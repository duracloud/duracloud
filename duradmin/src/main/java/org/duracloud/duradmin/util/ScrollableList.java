/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import java.util.List;
import java.util.Stack;

public abstract class ScrollableList<E>
        implements Scrollable<E> {

    private long maxResultsPerPage = 10;

    private List<E> resultList;

    private boolean markedForUpdate = true;
    

    private E currentMarker = null;

    /**
     * The markers represent the last item in each previous "page" of results.
     * The last element in the list refers to the marker for the current page.
     */
    //private Queue<E> markers = new LinkedList<E>();
    private Stack<E> markers = new Stack<E>();

    public long getMaxResultsPerPage() {
        return this.maxResultsPerPage;
    }

    public void markForUpdate(){
        this.markedForUpdate = true;
    }
    
    public void setMaxResultsPerPage(int maxResults) {
        if (this.maxResultsPerPage != maxResults) {
            this.maxResultsPerPage = maxResults;
            first();
        }
    }

    private E getLastResultInCurrentList() {
        List<E> results = this.resultList;
        if (results.size() > 0) {
            return results.get(results.size() - 1);
        } else {
            return null;
        }

    }

    public void next() {
        if (!isNextAvailable()) {
            return;
        }
        //put current marker in the marker stack.
        E previousMarker = this.currentMarker;
        if (previousMarker != null) {
            this.markers.push(previousMarker);
        }

        //set the currentMarker
        this.currentMarker = getLastResultInCurrentList();
        //flag for update.
        markedForUpdate = true;

        try {

            update();
        } catch (DataRetrievalException ex) {
            //rollback state
            if (previousMarker != null) {
                this.markers.pop();
            }
            this.currentMarker = previousMarker;
            throw new RuntimeException(ex);
        }

    }

    public void first() {
        this.markers.clear();
        this.currentMarker = null;
        this.markedForUpdate = true;
    }

    public void previous() {
        if (isPreviousAvailable()) {
            if (this.markers.size() > 0) {
                this.currentMarker = this.markers.pop();
            } else {
                this.currentMarker = null;
            }

            this.markedForUpdate = true;
        }
    }

    public boolean isPreviousAvailable() {
        return this.markers.size() > 0 || this.currentMarker != null ;
    }

    public boolean isNextAvailable() {
        try {
            update();
            return getResultList().size() >= this.maxResultsPerPage;
        } catch (DataRetrievalException e) {
            throw new RuntimeException(e);
        }
    }

    private E getCurrentMarker() {
        return this.currentMarker;
    }

    public List<E> getResultList() {
        try {
            update();
            return this.resultList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected final void update() throws DataRetrievalException {
        if (markedForUpdate) {
            this.resultList = getData(isPreviousAvailable() || this.currentMarker != null? this.currentMarker : null);
            markedForUpdate = false;
        }
    }

    protected abstract List<E> getData(E currentMarker) throws DataRetrievalException;

}
