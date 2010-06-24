/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import java.io.File;
import java.util.Iterator;

/**
 * Endpoint to which files are synchronized.
 *
 * @author: Bill Branan
 * Date: Mar 17, 2010
 */
public interface SyncEndpoint {

    /**
     * Ensures that the endpoint includes a file equivalent to the provided
     * local file:
     * - If the local file exists but no file with the same path exists at
     * the endpoint, the local file will be copied to the endpoint
     * - If the local file exists and is different from a file with the same
     * path at the endpoint, the local file will replace the file at the
     * endpoint.
     * - If the local file does not exist (it has been deleted), a file with 
     * the same path will be removed at the endpoint (if it exists)
     *
     * @param file local file to sync with the endpoint
     * @param watchDir the directory under watch in which this file resides
     * @return true if file was successfully synced, false otherwise
     */
    public boolean syncFile(File file, File watchDir);

    /**
     * Retrieves a listing of all files available at the endpoint. This list
     * is to be used for comparison with the local system.
     *
     * @return Iterator allowing access to complete file listing
     */
    public Iterator<String> getFilesList();
    
}
