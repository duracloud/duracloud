/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch;

/**
 * A listener class that reports activity of a stitching operation which,
 * depending on the size of the file as well as the speed and quality of the
 * network connection, can take potentially long periods of time.
 *
 * @author dbernstein
 */
public interface FileStitcherListener {
    /**
     * Indicates that the specified chunk has been stitched.
     *
     * @param chunkId the content id of the retrieved chunk
     */
    void chunkStitched(String chunkId);
}
