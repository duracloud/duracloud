/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

/**
 * A listener class that reports activity of a retrieval operation which,
 * depending on the size of the file as well as the speed and quality of the
 * network connection, can take potentially long periods of time.
 * 
 * @author dbernstein
 *
 */
public interface RetrievalListener {
    
    /**
     * Indicates that the specified chunk has been retrieved. This 
     * method is only called by chunked retrievals.
     * @param chunkId the content id of the retrieved chunk
     */
    void chunkRetrieved(String chunkId);
}
