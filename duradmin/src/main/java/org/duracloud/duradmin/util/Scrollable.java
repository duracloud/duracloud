/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import java.util.List;

/**
 * A simple interface for lists supporting scrollable behavior result sets.
 * 
 * @author Danny Bernstein
 * @version $Id$
 */
public interface Scrollable<E> {

    /**
     * The max number of results per page; ie the page size.
     * 
     * @return
     */
    public long getMaxResultsPerPage();

    public void setMaxResultsPerPage(int maxResults);

    public boolean isPreviousAvailable();

    public void first();

    public void previous();

    /**
     * Returns the results for the current "page"
     * 
     * @return
     */
    public List<E> getResultList();
}
