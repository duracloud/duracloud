/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch;

import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.domain.Content;
import org.duracloud.stitch.error.InvalidManifestException;

/**
 * This interface defines the contract of a FileStitcher.
 *
 * @author Andrew Woods
 * Date: 9/3/11
 */
public interface FileStitcher {

    /**
     * This method retrieves the original content item as defined by the chunks
     * manifest object found in the arg space-id and content-id.
     *
     * @param spaceId   of chunks manifest
     * @param contentId of chunks manifest
     * @return reconstituted content item defined in manifest
     * @throws InvalidManifestException if manifest file is named with improper
     *                                  naming convention, or there is an error
     *                                  retrieving the manifest.
     */
    default public Content getContentFromManifest(String spaceId, String contentId)
        throws InvalidManifestException {
        return getContentFromManifest(spaceId, contentId, null);
    }

    public Content getContentFromManifest(String spaceId, String contentId, FileStitcherListener listener)
        throws InvalidManifestException;

    /**
     * This method returns the deserialized ChunksManifest object found in the
     * arg spaceId with the arg manifestId.
     *
     * @param spaceId    of manifest content item
     * @param manifestId of manifest content item
     * @return deserialized ChunksManifest
     * @throws InvalidManifestException on error
     */
    public ChunksManifest getManifest(String spaceId, String manifestId)
        throws InvalidManifestException;
}
