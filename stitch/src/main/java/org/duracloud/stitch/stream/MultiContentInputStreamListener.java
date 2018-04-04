/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch.stream;

/**
 * A listener class that receives notification from a <code>MultiContentInputStream</code> as
 * each underlying content item is read.
 *
 * @author dbernstein
 */
public interface MultiContentInputStreamListener {

    /**
     * Called when a contentId has been completely read from the stream.
     *
     * @param contentId The contentId for the file that was just read.
     */
    void contentIdRead(String contentId);
}
